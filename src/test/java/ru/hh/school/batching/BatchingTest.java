package ru.hh.school.batching;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.hh.school.BaseTest;
import ru.hh.school.TestHelper;
import ru.hh.school.entity.Resume;

public class BatchingTest extends BaseTest {

  public static final int JDBC_BATCH_SIZE = 10;

  @BeforeClass
  public static void createTable() {
    TestHelper.executeScript(pg.getPostgresDatabase(), "create_resume.sql");
  }

  @Before
  public void clearTable() {
    TestHelper.execute(pg.getPostgresDatabase(), "delete from resume");
  }

  /**
   *
   * @see scripts/create_resume.sql
   *      и hibernate.properties (раздел batch processing)
   */
  @Test
  public void fewPersistsShouldBeCombinedIntoBatch() {
    doInTransaction(() -> {
      for (int i = 1; i <= 20; i++) {
        getSession().persist(new Resume("superResume" + i));

        if (i % JDBC_BATCH_SIZE == 0) {
          getSession().flush();
          getSession().clear();
        }
      }
    });

    assertEquals(2L, getInsertCount());
    // при правильной конфигурации в ru.hh.school.batching.Resume
    // в логах, в разделе Session Metrics, вы увидете "x nanoseconds spent executing y JDBC batches"
  }

}
