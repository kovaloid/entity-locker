package com.locker.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class EntityLocker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityLocker.class);

  private static final Set<Object> LOCKED_IDS = new HashSet<>();
  private static final Lock LOCK = new ReentrantLock();
  private static final Condition CONDITION = LOCK.newCondition();

  private EntityLocker() {
  }

  public static <T> void lock(T id, Runnable protectedTask) throws InterruptedException {
    lock(id, protectedTask, -1);
  }

  public static <T> void lock(T id, Runnable protectedTask, long timeout) throws InterruptedException {
    LOCK.lock();
    if (LOCKED_IDS.contains(id)) {
      LOGGER.info("Waiting for locked ID: " + id);
      if (timeout < 0) {
        while (LOCKED_IDS.contains(id)) {
          CONDITION.await();
        }
      } else {
        if (!CONDITION.await(timeout, TimeUnit.MILLISECONDS) || LOCKED_IDS.contains(id)) {
          LOGGER.info("Time is up for ID: " + id + " , timeout: " + timeout);
          LOCK.unlock();
          return;
        }
      }
      LOGGER.info("Released ID: " + id);

      try {
        // execution of protected code on the same entities
        protectedTask.run();
      } finally {
        LOCK.unlock();
      }
    } else {
      LOCKED_IDS.add(id);
      LOGGER.info("Locked new ID: " + id);
      LOCK.unlock();

      try {
        // concurrent execution of protected code on different entities
        protectedTask.run();
      } finally {
        LOCK.lock();
        LOCKED_IDS.remove(id);
        LOGGER.info("Unlocked ID: " + id);
        CONDITION.signalAll();
        LOCK.unlock();
      }
    }
  }
}
