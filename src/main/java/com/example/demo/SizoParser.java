package com.example.demo;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    public static LocalDate targetDate = LocalDate.of(2025, 1, 11); 

    public SizoParser(TelegramParserBot telegramParserBot) throws IOException {
        this.telegramParserBot = telegramParserBot;
        String jarDir = new File(SizoParser.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .getParent();
        String driverPath = jarDir + File.separator + "chromedriver";
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            TelegramParserBot.admin = Files.readAllLines(Paths.get(jarDir + File.separator + "admins.txt")).get(0);
        }
        catch (Exception e) {
        }

    }
    @Scheduled(fixedRate = 90)
    public void sendRequestParser() throws InterruptedException {
        
        if (turnedOn && !freeOrdered) {
            
            if (orderDate != null && !orderDate.isEmpty()) {
                try {
                    targetDate = LocalDate.parse(orderDate); 
                } catch (DateTimeParseException e) {
                }
            }

            String targetDateStr = targetDate.toString();

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='clientzone_name']")));
            } catch (TimeoutException e) {
                updateLogin();
                System.out.println("Вход!");
                return;
            }
            String url = "https://f-okno.ru/base/moscow/medved?sub=order&date=" + targetDateStr + "&t=10";

            try {
                driver.get(url);
                Select select = new Select(driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/p[1]/select")));
                if (!select.getOptions().isEmpty()) {
                    try {
                        orderDate = targetDateStr;

                        select = new Select(driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/p[1]/select")));
                        orderTime = select.getFirstSelectedOption().getText();

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
                }
            } catch (NoSuchElementException e) {

            }
        }
    }

    @Async
    @Scheduled(fixedRate = 300000) 
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

    @Scheduled(fixedRate = 300000)
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
