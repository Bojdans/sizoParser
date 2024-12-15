package com.example.demo;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Duration;
import java.util.List;

@Service
public class SizoParser {
    private final TelegramParserBot telegramParserBot;
    private final String login = "macrocosm.0.x@gmail.com";
    private final String password = "FFss33iinn";
    private final WebDriver driver;
    private final WebDriverWait wait;
    private boolean signedIn = false;
    public static String orderDate = "";
    public static String orderTime = "";
    public static boolean turnedOn = false;
    public static boolean freeOrdered = false;

    public SizoParser(TelegramParserBot telegramParserBot) {
        this.telegramParserBot = telegramParserBot;
        System.setProperty("webdriver.chrome.driver", "chromeDriver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Async
    @Scheduled(fixedRate = 500)
    public void sendRequestParser() throws InterruptedException {
        if (turnedOn && !freeOrdered) {
//                driver.get("https://f-okno.ru/base/moscow/kp24olenegorsk?order_type=1");
            driver.get("https://f-okno.ru/base/moscow/medved?order_type=1");
            if (driver.getCurrentUrl().contains("login")) {
                updateLogin();
            }
            boolean isFreeWindowFound = false;

            // Цикл поиска окна
            while (!isFreeWindowFound && turnedOn) {
                List<WebElement> freeDateElements = driver.findElements(By.className("free"));
                if (!freeDateElements.isEmpty()) {
                    try {
                        isFreeWindowFound = true; // Найдено свободное окно
                        WebElement freeDate = freeDateElements.get(0); // Получаем первый элемент из списка
                        orderDate = freeDate.getText().replace("\n", " ").trim().replace("Есть места", "");
                        Thread.sleep(100);
                        freeDate.click();
                        Select select = new Select(driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/p[1]/select")));
                        orderTime = select.getAllSelectedOptions().get(0).getText();
                        select.selectByIndex(0);

                        WebElement checkbox = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[1]/div/input"));
                        checkbox.click();

                        WebElement submitButton = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[2]/a"));
                        submitButton.click();
                        freeOrdered = true;
                    } finally {
                        telegramParserBot.sendMessage(telegramParserBot.admin, "Окно отловлено\nДата: " + orderDate + "\nВремя: " + orderTime);
                        turnedOn = false;
                    }
                } else {
                    Thread.sleep(500);
                    driver.get("https://f-okno.ru/base/moscow/medved?order_type=1");
                }
            }
        }
    }

    @Async
    @Scheduled(fixedRate = 300000) // Вход каждые 5 минут
    public void updateLogin() {
        try {
            this.signedIn = false;
            driver.get("https://f-okno.ru/login");
            if (driver.getCurrentUrl().equals("https://f-okno.ru/login")) {
                WebElement loginField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[1]/input")));
                loginField.sendKeys(login);

                WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[2]/input")));
                passwordField.sendKeys(password);

                WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[3]/a")));
                signInButton.click();
                this.signedIn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void onShutdown() {
        driver.quit();
    }

    @Bean
    public String setBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramParserBot);
            System.out.println("Бот успешно зарегистрирован!");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при регистрации бота: " + e.getMessage());
        }
        return "";
    }
}
