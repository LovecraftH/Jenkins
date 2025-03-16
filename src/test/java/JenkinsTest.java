import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JenkinsTest {
    private static final Logger logger = LoggerFactory.getLogger(JenkinsTest.class);

    @Test
    @Tag("UI")
    public void first() {
        logger.info("Тест номер 1 UI");
        System.out.println("SYSTEM Тест номер 1 UI");
        Assertions.assertTrue(true);

    }

    @Test
    @Tag("UI")
    public void twoFalse() {
        logger.info("Тест номер 2 UI");
        System.out.println("SYSTEM Тест номер 2 UI");
        Assertions.assertTrue(false);
    }

    @Test
    @Tag("API")
    public void threeTrue() {
        logger.info("Тест номер 3 API");
        System.out.println("SYSTEM тест номер 3 API");
        Assertions.assertTrue(true);
    }


}
