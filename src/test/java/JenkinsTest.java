import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JenkinsTest {

    @Test
    public void first() {
        System.out.println("Тест номер 1");
        Assertions.assertTrue(true);
    }


    @Test
    public void twoFalse() {
        System.out.println("Тест номер 2");
        Assertions.assertTrue(false);
    }


    @Test
    public void threeTrue() {
        System.out.println("Тест номер 3");
        Assertions.assertTrue(true);
    }


}
