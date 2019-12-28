import java.time.LocalDate;

/**
 * @Author lzc
 * @Date 2019/12/28 10:36
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(LocalDate.parse("2019-11-01").minusDays(1).toString());
    }
}
