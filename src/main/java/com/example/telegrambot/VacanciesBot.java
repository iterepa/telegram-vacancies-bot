package com.example.telegrambot;

import com.example.telegrambot.dto.VacancyDto;
import com.example.telegrambot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {

    @Autowired
    private VacancyService vacancyService;

    private final Map<Long, String> lastShownVacanciesLevel = new HashMap<>();

    private static final String token = "6666288870:AAHeVcU3uY73UwZ2yvLVsecDAJTHhRwg0Pc";


    public VacanciesBot() {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() != null){
            handleStartCommand(update);
        }
        if(update.getCallbackQuery() != null) {
            String callbackData = update.getCallbackQuery().getData();

            if("showJuniorVacancies".equals(callbackData)) {
                try {
                    showJuniorVacancies(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if("showMiddleVacancies".equals(callbackData)){
                try {
                    showMiddleVacancies(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if("showSeniorVacancies".equals(callbackData)){
                try {
                    showSeniorVacancies(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if(callbackData.startsWith("vacancyId=")){
                String id = callbackData.split("=")[1];
                try {
                    showVacancyDescription(id, update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.startsWith("backToVacancies")){
                try {
                    handleBackToVacanciesCommand(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            } else if (callbackData.startsWith("backToStartMenu")){
                try {
                    handlerBackToStartMenuCommand(update);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handlerBackToStartMenuCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage= new SendMessage();
        sendMessage.setText("Choose Title:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }

    private void handleBackToVacanciesCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String level = lastShownVacanciesLevel.get(chatId);

        if("junior".equals(level)){
            showJuniorVacancies(update);
        } else if ("middle".equals(level)) {
            showMiddleVacancies(update);
        } else if ("senior".equals(level)) {
            showSeniorVacancies(update);
        }
    }

    private void showSeniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacanciesLevel.put(chatId, "senior");
    }

    private ReplyKeyboard getVacanciesMenu(List<VacancyDto> vacancies) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (VacancyDto vacancyDto: vacancies){
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancyDto.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancyDto.getId());
            row.add(vacancyButton);
        }
        return new InlineKeyboardMarkup(List.of(row));
    }
    private ReplyKeyboard getSeniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getSeniorVacancies();

        return getVacanciesMenu(vacancies);
    }

    private void showMiddleVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please choose vacancy");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);

        lastShownVacanciesLevel.put(chatId, "middle");

    }

    private ReplyKeyboard getMiddleVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getMiddleVacancies();

        return getVacanciesMenu(vacancies);
    }

    private void showVacancyDescription(String id, Update update) throws TelegramApiException {
        VacancyDto vacancy = vacancyService.get(id);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        String vacancyInfo = """
                *Title:* %s
                *Company:* %s
                *Short Description:* %s
                *Description:* %s
                *Salary:* %s
                *Link:* [%s](%s)
                """.formatted(escapeMarkdownReservedChars(vacancy.getTitle()),
                escapeMarkdownReservedChars(vacancy.getCompany()),
                escapeMarkdownReservedChars(vacancy.getShortDescription()),
                escapeMarkdownReservedChars(vacancy.getLongDescription()),
                vacancy.getSalary().isBlank() ? "Not specified" : escapeMarkdownReservedChars(vacancy.getSalary()),
                "Click here",
                escapeMarkdownReservedChars(vacancy.getLink())
        );

        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);
    }

    private String escapeMarkdownReservedChars(String text) {
        return text.replace("-", "\\-")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    private ReplyKeyboard getBackToVacanciesMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenu = new InlineKeyboardButton();
        backToStartMenu.setText("Back to start menu");
        backToStartMenu.setCallbackData("backToStartMenu");
        row.add(backToStartMenu);

        return new InlineKeyboardMarkup(List.of(row));
    }
    private void showJuniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setText("Please choose vacancy");
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacanciesLevel.put(chatId, "junior");
    }

    private ReplyKeyboard getJuniorVacanciesMenu() {
        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();

        return getVacanciesMenu(vacancies);
    }

    private void handleStartCommand(Update update) {
        String text = update.getMessage().getText();
        System.out.println("event received " + text);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Welcome to bot. Choose your title: ");
        sendMessage.setReplyMarkup(getStartMenu());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior");
        junior.setCallbackData("showJuniorVacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("Middle");
        middle.setCallbackData("showMiddleVacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("Senior");
        senior.setCallbackData("showSeniorVacancies");
        row.add(senior);

         return new InlineKeyboardMarkup(List.of(row));
    }

    @Override
    public String getBotUsername() {
        return "test vacancies bot";
    }
}
