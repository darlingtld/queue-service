package com.improbable.queue.service;

import com.improbable.queue.model.UserStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class QueueService {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private static final List<UserStub> QUEUE = new LinkedList<>();
    private static final Map<String, UserStub> LOOKUP_MAP = new HashMap<>();

    @Autowired
    private IdentifierService identifierService;

    public String join(UserStub mockObject) {
        try {
            if (writeLock.tryLock()) {
                String identifier = identifierService.generateIdentifier();
                mockObject.setIdentifier(identifier);
                QUEUE.add(mockObject);
                LOOKUP_MAP.put(identifier, mockObject);
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
                return LOOKUP_MAP.containsKey(identifier);
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public int getPositionInQueue(String identifier) {
        try {
            if (readLock.tryLock()) {
                int position = 0;
                for (UserStub object : QUEUE) {
                    if (object.getIdentifier().equals(identifier)) {
                        return position;
                    }
                    position++;
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
                if (QUEUE.isEmpty()) {
                    return false;
                }
                return Boolean.TRUE.equals(QUEUE.get(getPositionInQueue(identifier)).getIsAdmittable());
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
                LOOKUP_MAP.remove(identifier);
                QUEUE.remove(position);
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
                return QUEUE.isEmpty();
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public UserStub getFirst() {
        try {
            if (readLock.tryLock()) {
                assert QUEUE.size() > 0;
                return QUEUE.get(0);
            }
        } finally {
            readLock.unlock();
        }
        throw new RuntimeException("please retry later");
    }

    public void clear(){
        try {
            if (writeLock.tryLock()) {
                QUEUE.clear();
            }
        } finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
            }
        }
    }
}
