package com.improbable.queue.service;

import com.improbable.queue.model.UserStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class QueueService {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private static int MIN_QUEUE_INDEX = 0;
    private static int MAX_QUEUE_INDEX = 0;
    private static final int MAX_SUB_QUEUE_SIZE = 1000;
    private static final Map<Integer, ConcurrentLinkedQueue<UserStub>> QUEUES = new ConcurrentHashMap<>();
    private static final Map<String, UserStub> IDENTIFIER_LOOKUP_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Integer> IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP = new ConcurrentHashMap<>();

    @Autowired
    private IdentifierService identifierService;

    @PostConstruct
    private void init() {
        QUEUES.put(MIN_QUEUE_INDEX, new ConcurrentLinkedQueue<>());
    }

    public String join(UserStub userStub) {
        try {
            if (writeLock.tryLock()) {
                String identifier = identifierService.generateIdentifier();
                userStub.setIdentifier(identifier);

                ConcurrentLinkedQueue<UserStub> maxQueue = QUEUES.get(MAX_QUEUE_INDEX);
                if (maxQueue.size() >= MAX_SUB_QUEUE_SIZE) {
                    MAX_QUEUE_INDEX++;
                    ConcurrentLinkedQueue<UserStub> newQueue = new ConcurrentLinkedQueue<>();
                    QUEUES.put(MAX_QUEUE_INDEX, newQueue);
                    maxQueue = newQueue;
                }
                userStub.setQueueIndex(MAX_QUEUE_INDEX);
                maxQueue.add(userStub);

                IDENTIFIER_LOOKUP_MAP.put(identifier, userStub);
                IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP.put(identifier, MAX_QUEUE_INDEX);
                return identifier;
            }
        } finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
        throw new RuntimeException("please retry later");
    }

    public boolean checkExists(String identifier) {
        try {
            if (readLock.tryLock()) {
                return IDENTIFIER_LOOKUP_MAP.containsKey(identifier);
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public int getPositionInQueue(String identifier) {
        try {
            if (readLock.tryLock()) {
                if (IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP.containsKey(identifier)) {
                    int queueIndex = IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP.get(identifier);
                    ConcurrentLinkedQueue<UserStub> identifierQueue = QUEUES.get(queueIndex);
                    int positionInQueue = 0;
                    for (UserStub userStub : identifierQueue) {
                        if (userStub.getIdentifier().equals(identifier)) {
                            break;
                        }
                        positionInQueue++;
                    }
                    for (int i = MIN_QUEUE_INDEX; i <= queueIndex; i++) {
                        if (i == queueIndex) {
                            continue;
                        }
                        positionInQueue += QUEUES.get(i).size();
                    }
                    return positionInQueue;
                }
                return -1;
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");

    }

    public boolean isIdentifierAdmittable(String identifier) {
        try {
            if (readLock.tryLock()) {
                return IDENTIFIER_LOOKUP_MAP.getOrDefault(identifier, UserStub.builder().isAdmittable(false).build()).isAdmittable();
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public boolean removeIdentifier(String identifier) {
        try {
            if (writeLock.tryLock()) {
                int position = getPositionInQueue(identifier);
                if (position < 0) {
                    return false;
                }
                ConcurrentLinkedQueue<UserStub> identifierQueue = QUEUES.get(IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP.get(identifier));
                UserStub userStubToDelete = null;
                for (UserStub userStub : identifierQueue) {
                    if (userStub.getIdentifier().equals(identifier)) {
                        userStubToDelete = userStub;
                        break;
                    }
                }
                identifierQueue.remove(userStubToDelete);
                IDENTIFIER_LOOKUP_MAP.remove(identifier);
                IDENTIFIER_QUEUE_INDEX_LOOKUP_MAP.remove(identifier);
                return true;
            }
        } finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
        return false;
    }

    public boolean isQueueEmpty() {
        try {
            if (readLock.tryLock()) {
                for (int i = MIN_QUEUE_INDEX; i <= MAX_QUEUE_INDEX; i++) {
                    if (!QUEUES.get(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public UserStub getFirst() {
        try {
            if (readLock.tryLock()) {
                assert QUEUES.get(MIN_QUEUE_INDEX).size() > 0;
                return QUEUES.get(MIN_QUEUE_INDEX).peek();
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public void clear() {
        try {
            if (writeLock.tryLock()) {
                QUEUES.clear();
                MIN_QUEUE_INDEX = 0;
                MAX_QUEUE_INDEX = 0;
                QUEUES.put(MIN_QUEUE_INDEX, new ConcurrentLinkedQueue<>());
            }
        } finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }

    public long size() {
        try {
            long size = 0;
            if (readLock.tryLock()) {
                for (int i = MIN_QUEUE_INDEX; i <= MAX_QUEUE_INDEX; i++) {
                    size += QUEUES.get(i).size();
                }
                return size;
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }
}
