
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;


@Slf4j
public class JenkinsTest {


    @Test
    @Tag("UI")
    public void first() {
        log.info("Тест номер 1 UI");
        Assertions.assertTrue(true);

    }

    @Test
    @Tag("UI")
    public void twoFalse() {
        log.info("Тест номер 2 UI");
        Assertions.assertTrue(false);
    }

    @Test
    @Tag("API")
    public void threeTrue() {
        log.info("Тест номер 3 API");
        Assertions.assertTrue(true);
    }


}
