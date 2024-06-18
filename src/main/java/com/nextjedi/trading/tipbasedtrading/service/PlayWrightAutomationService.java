package com.nextjedi.trading.tipbasedtrading.service;

import com.atlassian.onetime.core.TOTPGenerator;
import com.atlassian.onetime.model.TOTPSecret;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PlayWrightAutomationService {
    public String generateToken(String apiKey,String userId,
                               String password,String totpKey){
        TOTPSecret totpSecret = TOTPSecret.Companion.fromBase32EncodedString(totpKey);
        TOTPGenerator totpGenerator = new TOTPGenerator();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://kite.trade/connect/login?api_key="+apiKey+"&v=3");
            page.locator("#userid").fill(userId);
            page.locator("#password").fill(password);
            page.locator("#container > div > div > div.login-form > form > div.actions > button").click();
//            todo more logical time out
            page.waitForTimeout(1000);
//            todo improve locator
            var totp = totpGenerator.generateCurrent(totpSecret);
            page.locator("xpath=//*[@id=\"userid\"]").fill(totp.getValue());
            page.waitForURL(Pattern.compile(".*token"));
            log.info(page.url());
//            todo improve URI parser
            var query =URI.create(page.url()).getQuery();
            var res =query.split("request_token=");
            return res[1];
        }
    }
}
