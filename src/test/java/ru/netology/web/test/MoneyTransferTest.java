package ru.netology.web.test;

import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;

import static java.nio.channels.FileChannel.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.web.data.DataHelper.*;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup() {
        var loginPage = ("http://localhost:9999", loginPage.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo =DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        secondCardBalance = dashboardPage.getCardBalace(1);
    }

    @Test
    void shouldTransferFromFirstToSecond() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance + amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalace(1);
        assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания");
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber()));
        var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber()));
        assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(secondCardBalance, actualBalanceSecondCard);
    }
}