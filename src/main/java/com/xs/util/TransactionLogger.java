package com.xs.util;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TransactionLogger {
	@Autowired File file;
	public void writeLog(String str) {
		try {
			Files.append(
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
					.withLocale(Locale.US)
					.withZone(ZoneId.systemDefault())
					.format(Instant.now())
					+ " " + str + "\n",
				file, Charset.defaultCharset()
			);
		} catch (IOException ex) {
			log.error("fail to write log", ex);
		}
	}
}
