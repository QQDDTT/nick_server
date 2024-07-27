package com.dream.nick_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NickServerApplication {

	public static void main(String[] args) {
		// 确保你的终端支持 ANSI 转义序列，以显示彩色输出。
		AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
		SpringApplication.run(NickServerApplication.class, args);
	}

}
