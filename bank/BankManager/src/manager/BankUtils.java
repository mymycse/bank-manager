package manager;

import java.text.NumberFormat;

public class BankUtils {
    public static String displayAccountNo(String accountNo) {
        return accountNo.replaceAll("(\\d{4})(\\d{1})(\\d{4})", "$1-$2-$3");
    }
    public static String removeHyphens(String formattedAccountNo) {
        // 정규식으로 하이픈 제거
        return formattedAccountNo.replaceAll("-", "");
    }

    public static String displayBalance(long balance) {
        return NumberFormat.getInstance().format(balance);
    }

}