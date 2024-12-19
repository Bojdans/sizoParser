package com.example.demo;

import jakarta.annotation.PreDestroy;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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

import java.io.File;
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
        String jarDir = new File(SizoParser.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                .getParent();
        String driverPath = jarDir + File.separator + "chromedriver";
        System.setProperty("webdriver.chrome.driver", driverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Scheduled(fixedRate = 300)
    public void sendRequestParser() throws InterruptedException {
        // Проверка включения парсера и того, что окно ещё не отловлено
        if (turnedOn && !freeOrdered) {
            // Открываем страницу
            driver.get("https://f-okno.ru/base/moscow/medved?order_type=1");

            // Проверка, что пользователь залогинен через наличие элемента clientzone_name
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='clientzone_name']")));
            } catch (TimeoutException e) {
                // Если пользователь не залогинен, обновляем логин и пробуем заново
                updateLogin();
                Thread.sleep(300); // Ждём некоторое время, чтобы данные обновились
                System.out.println("Пользователь не залогинен. Выполняется вход...");
                return; // Завершаем выполнение метода, чтобы не продолжать парсинг
            }

            boolean isFreeWindowFound = false;

            // Цикл поиска свободного окна
            while (!isFreeWindowFound && turnedOn) {
                List<WebElement> freeDateElements = driver.findElements(By.className("free"));

                if (!freeDateElements.isEmpty()) {
                    try {
                        isFreeWindowFound = true; // Найдено свободное окно

                        // Получаем последний доступный элемент со свободной датой
                        WebElement freeDate = freeDateElements.get(freeDateElements.size() - 1);
                        orderDate = freeDate.getText().replace("\n", " ").trim().replace("Есть места", "");
                        Thread.sleep(100);

                        // Кликаем по найденному элементу
                        freeDate.click();

                        // Выбор времени через выпадающий список
                        Select select = new Select(driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/p[1]/select")));
                        orderTime = select.getFirstSelectedOption().getText();
                        select.selectByIndex(0);

                        // Кликаем по чекбоксу
                        WebElement checkbox = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[1]/div/input"));
                        checkbox.click();

                        // Кнопка подтверждения
                        WebElement submitButton = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/form/div[2]/a"));
                         submitButton.click(); // Раскомментируйте для реального подтверждения
                        System.out.println("окно отловлено");
                        freeOrdered = true; // Отмечаем, что свободное окно отловлено
                    } finally {
                        // Отправляем сообщение через Telegram бота
                        telegramParserBot.sendMessage(telegramParserBot.admin, "Окно отловлено\nДата: " + orderDate + "\nВремя: " + orderTime);
                        turnedOn = false; // Выключаем парсер после успешной обработки
                    }
                } else {
                    // Если свободных окон нет, обновляем страницу

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
