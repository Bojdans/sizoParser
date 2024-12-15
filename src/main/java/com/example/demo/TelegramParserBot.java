package com.example.demo;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramParserBot extends TelegramLongPollingBot {
    private final String BOT_TOKEN = "7753342824:AAE-tXNgmY31qICBF0fXvqp7poOfWSgoquE";
    private final String BOT_USERNAME = "sizoParserBot";
    public String admin = "1144390849";
    public String password = "XSKW-FDLAS-2DSk";
    private static long lastUpdateId = -1; // Хранит ID последнего обработанного обновления

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что это новое обновление
        if (update.getUpdateId() == lastUpdateId) {
            return; // Пропускаем уже обработанное обновление
        }
        lastUpdateId = update.getUpdateId();

        // Ваш код обработки обновлений
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            // Обработка команд
            switch (messageText) {
                case "Установить админа":
                    sendMessage(chatId, "Введите пароль");
                    break;
                case "/start":
                    sendMessage(chatId, "Здравствуйте!");
                    break;
                case "Включить парсер":
                    if (admin.equals(chatId)) {
                        sendMessage(chatId, "Парсер включён");
                        SizoParser.freeOrdered = false;
                        SizoParser.turnedOn = true;
                    }
                    else{
                        sendMessage(chatId,"вы не администратор!");
                    }
                    break;
                case "Выключить парсер":
                    if (admin.equals(chatId)) {
                        sendMessage(chatId, "Парсер выключён");
                        SizoParser.turnedOn = false;
                    }
                    else{
                        sendMessage(chatId,"вы не администратор!");
                    }
                    break;
                case "Получить статус":
                    if (chatId.equals(admin)) {
                        if (SizoParser.freeOrdered) {
                            sendMessage(admin, "Окно отловлено\n" + "дата:" + SizoParser.orderDate + "\n" + "время:" + SizoParser.orderTime);
                        } else {
                            sendMessage(admin, "Окно ещё не отловлено!!");
                        }
                    }
                    else{
                        sendMessage(chatId,"вы не администратор!");
                    }
                    break;
                case "XSKW-FDLAS-2DSk":
                    admin = chatId;
                    sendMessage(chatId, "Админ: " + admin + "(вы)");
                    break;
                default:
                    sendMessage(chatId, "Команда не распознана.");
                    break;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        sendReplyKeyboard(message);
    }

    public void sendReplyKeyboard(SendMessage message) {
        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        keyboardMarkup.setResizeKeyboard(true); // Подгоняем размер кнопок
        keyboardMarkup.setOneTimeKeyboard(false); // Клавиатура остаётся видимой после выбора кнопки

        // Создаем ряды кнопок
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Установить админа"));

        // Второй ряд кнопок
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Включить парсер"));
        row2.add(new KeyboardButton("Выключить парсер"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Получить статус"));

        // Добавляем ряды в клавиатуру
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        // Устанавливаем клавиатуру
        keyboardMarkup.setKeyboard(keyboard);

        // Устанавливаем клавиатуру в сообщение
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message); // Отправляем сообщение
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}