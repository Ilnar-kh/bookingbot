package com.bookingbot;

import com.bookingbot.bot.Bot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class BookingbotApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(BookingbotApplication.class, args);

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(context.getBean(Bot.class));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
