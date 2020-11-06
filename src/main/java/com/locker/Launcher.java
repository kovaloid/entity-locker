package com.locker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.locker.utils.EntityLocker;


public class Launcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

  public static void main(String[] args) {
    createNewThread("1", "[PROCESSING] entity with ID = 1 (1)", 10000);
    createNewThread("1", "[PROCESSING] entity with ID = 1 (2)", 0);
    createNewThread("1", "[PROCESSING] entity with ID = 1 (3)", 10000);
    createNewThread("1", "[PROCESSING] entity with ID = 1 (4)", 20000);

    createNewThread(2, "[PROCESSING] entity with ID = 2", 0);
    createNewThread(3, "[PROCESSING] entity with ID = 3", 0);
    createNewThread(4, "[PROCESSING] entity with ID = 4", 0);
    createNewThread(5, "[PROCESSING] entity with ID = 4", 0);
  }

  private static <T> void createNewThread(T id, String printedText, long timeout) {
    new Thread(() -> {
      try {
        EntityLocker.lock(id, () -> {
          LOGGER.info(printedText);
          delay();
        }, timeout);
      } catch (InterruptedException e) {
        LOGGER.warn("Entity locker was interrupted", e);
      }
    }).start();
  }

  private static void delay() {
    try {
      Thread.sleep(7000);
    } catch (InterruptedException e) {
      LOGGER.warn("The waiting was interrupted", e);
    }
  }
}
