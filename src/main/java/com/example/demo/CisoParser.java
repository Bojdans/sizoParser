package com.example.demo;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Duration;
import java.util.Objects;

//@Service
//public class CisoParser {
//
//    private final TelegramParserBot telegramParserBot;
//    private static final Logger log = LoggerFactory.getLogger(CisoParser.class);
//    private final String login = "macrocosm.0.x@gmail.com";
//    private final String password = "FFss33iinn";
//    private final WebDriver driver;
//    private final WebDriverWait wait;
//    private boolean signedIn = false;
//    private boolean messageSent = false; // Флаг для контроля отправки сообщения
//    public String orderDate = "";
//    public String orderTime = "";
//    public static boolean turnedOn = false;
//    @Autowired
//    public CisoParser(TelegramParserBot telegramParserBot) {
//        this.telegramParserBot = telegramParserBot;
//        System.setProperty("webdriver.chrome.driver", "chromeDriver/chromedriver.exe");
//        ChromeOptions options = new ChromeOptions();
//        this.driver = new ChromeDriver(options);
//        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//    }
//
//    @Async
//    @Scheduled(fixedRate = 100) // Периодический запуск каждые 10 миллисекунд
//    public void sendRequestParser() {
//            if (turnedOn) {
//                if (signedIn) {
//                    log.debug("парсер начал работу");
//                    driver.get("https://f-okno.ru/base/moscow/kp24olenegorsk?order_type=1");
//                    WebElement freeDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("free")));
//                    orderDate = freeDate.findElement(By.className("graphic_item_date")).getText().replace("\n", " ").trim();
//                    freeDate.click();
//                    Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/p[1]/select"))));
//                    orderTime = select.getAllSelectedOptions().get(0).getText();
//                    select.selectByIndex(0);
//                    WebElement checkbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("personal_agree")));
//                    checkbox.click();
//                    WebElement submitButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[5]/div[2]/a")));
//                    if (!messageSent) {
//                        telegramParserBot.sendMessage(telegramParserBot.admin,"Окно отловлено\n" + "дата:"+ orderDate + "время:" + orderTime);
//                        messageSent = true;
//                    }
//                }
//            }
//        }
//    @Async
//    @Scheduled(fixedRate = 300000)
//    public void updateLogin() throws InterruptedException {
//        this.signedIn = false;
//        driver.get("https://f-okno.ru/login");
//        if (Objects.equals(driver.getCurrentUrl(), "https://f-okno.ru/login")) {
//            signIn();
//        }
//    }
//
//    private void signIn() {
//        WebElement loginField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[1]/input")));
//        loginField.sendKeys(login);
//        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[2]/input")));
//        passwordField.sendKeys(password);
//        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[3]/a")));
//        signInButton.click();
//        this.signedIn = true;
//    }
//
//    @PreDestroy
//    public void onShutdown() {
//        driver.quit();
//    }
//
//    @Bean
//    public String setBot() {
//        try {
//            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            botsApi.registerBot(telegramParserBot);
//            System.out.println("Бот успешно зарегистрирован!");
//        } catch (TelegramApiException e) {
//            System.err.println("Ошибка при регистрации бота: " + e.getMessage());
//        }
//        return "";
//    }
//}