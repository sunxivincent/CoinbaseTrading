package com.xs.util;

import com.google.common.io.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Component
public class TransactionLogger {

	@Autowired File file;

	public void writeLog(String str) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		Files.append(
			DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
				.withLocale(Locale.US)
				.withZone(ZoneId.systemDefault())
				.format(Instant.now())
				+ " " + str + "\n",
			file, Charset.defaultCharset()
		);
	}
}
