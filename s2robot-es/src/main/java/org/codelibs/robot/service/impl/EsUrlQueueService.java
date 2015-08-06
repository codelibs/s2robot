package org.codelibs.robot.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.codelibs.core.lang.StringUtil;
import org.codelibs.robot.Constants;
import org.codelibs.robot.entity.AccessResult;
import org.codelibs.robot.entity.EsUrlQueue;
import org.codelibs.robot.entity.UrlQueue;
import org.codelibs.robot.service.UrlQueueService;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EsUrlQueueService extends AbstractRobotService implements UrlQueueService<EsUrlQueue> {
    private static final Logger logger = LoggerFactory.getLogger(EsUrlQueueService.class);

    @Resource
    protected EsDataService dataService;

    protected Queue<UrlQueue> crawlingUrlQueue = new ConcurrentLinkedQueue<UrlQueue>();

    public int pollingFetchSize = 20;

    public int maxCrawlingQueueSize = 100;

    @PostConstruct
    public void init() {
        esClient.addOnConnectListener(() -> {
            createMapping("queue");
        });
    }

    @Override
    public void updateSessionId(final String oldSessionId, final String newSessionId) {
        // TODO Script Query
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(final String sessionId, final String url) {
        final EsUrlQueue urlQueue = new EsUrlQueue();
        urlQueue.setSessionId(sessionId);
        urlQueue.setUrl(url);
        urlQueue.setCreateTime(System.currentTimeMillis());
        urlQueue.setLastModified(0L);
        urlQueue.setDepth(0);
        urlQueue.setMethod(Constants.GET_METHOD);
        insert(urlQueue);
    }

    @Override
    public void insert(final EsUrlQueue urlQueue) {
        urlQueue.setId(hashCodeAsLong(super.insert(urlQueue, urlQueue.getId() == null ? OpType.CREATE : OpType.INDEX)));
    }

    @Override
    public void delete(final String sessionId) {
        deleteBySessionId(sessionId);
    }

    @Override
    public void offerAll(final String sessionId, final List<EsUrlQueue> urlQueueList) {
        if (logger.isDebugEnabled()) {
            logger.debug("Offering URL: Session ID: {}, UrlQueue: {}", sessionId, urlQueueList);
        }
        final List<UrlQueue> targetList = new ArrayList<UrlQueue>(urlQueueList.size());
        for (final UrlQueue urlQueue : urlQueueList) {
            if (!exists(sessionId, urlQueue.getUrl()) && !dataService.exists(sessionId, urlQueue.getUrl())) {
                urlQueue.setSessionId(sessionId);
                targetList.add(urlQueue);
            } else if (logger.isDebugEnabled()) {
                logger.debug("Existed URL: Session ID: {}, UrlQueue: {}", sessionId, urlQueue);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Offered URL: Session ID: {}, UrlQueue: {}", sessionId, targetList);
        }
        if (!targetList.isEmpty()) {
            insertAll(targetList, OpType.CREATE);
        }
    }

    @Override
    public EsUrlQueue poll(final String sessionId) {
        final List<EsUrlQueue> urlQueueList =
                getList(EsUrlQueue.class, sessionId, null, 0, pollingFetchSize, SortBuilders.fieldSort(CREATE_TIME).order(SortOrder.ASC));
        if (urlQueueList.isEmpty()) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Queued URL: {}", urlQueueList);
        }
        for (final EsUrlQueue urlQueue : urlQueueList) {
            final String url = urlQueue.getUrl();
            if (crawlingUrlQueue.size() > maxCrawlingQueueSize) {
                return null;
            }
            if (super.delete(sessionId, url)) {
                crawlingUrlQueue.add(urlQueue);
                return urlQueue;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Already Deleted: {}", urlQueue);
            }
        }
        return null;
    }

    @Override
    public void saveSession(final String sessionId) {
        // TODO use cache
    }

    @Override
    public boolean visited(final EsUrlQueue urlQueue) {
        final String url = urlQueue.getUrl();
        if (StringUtil.isBlank(url)) {
            if (logger.isDebugEnabled()) {
                logger.debug("URL is a blank: " + url);
            }
            return false;
        }

        final String sessionId = urlQueue.getSessionId();
        if (super.exists(sessionId, url)) {
            return true;
        }

        final AccessResult accessResult = dataService.getAccessResult(sessionId, url);
        if (accessResult != null) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean exists(final String sessionId, final String url) {
        final boolean ret = super.exists(sessionId, url);
        if (!ret) {
            for (final UrlQueue urlQueue : crawlingUrlQueue) {
                if (sessionId.equals(urlQueue.getSessionId()) && url.equals(urlQueue.getUrl())) {
                    return true;
                }
            }
        }
        return ret;
    }

    @Override
    public void generateUrlQueues(final String previousSessionId, final String sessionId) {
        dataService.iterate(previousSessionId, accessResult -> {
            final EsUrlQueue urlQueue = new EsUrlQueue();
            urlQueue.setSessionId(sessionId);
            urlQueue.setMethod(accessResult.getMethod());
            urlQueue.setUrl(accessResult.getUrl());
            urlQueue.setParentUrl(accessResult.getParentUrl());
            urlQueue.setDepth(0);
            urlQueue.setLastModified(accessResult.getLastModified());
            urlQueue.setCreateTime(System.currentTimeMillis());
            insert(urlQueue);
        });
    }

}
