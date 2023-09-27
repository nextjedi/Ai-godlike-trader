package com.nextjedi.trading.tipbasedtrading.service;

import com.atlassian.onetime.core.TOTPGenerator;
import com.atlassian.onetime.model.TOTPSecret;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.stereotype.Component;

@Component
public class PlayWrightAutomationService {
    public String generateToken(String apiKey,String userId,
                               String password,String totpKey){
        TOTPSecret totpSecret = TOTPSecret.Companion.fromBase32EncodedString(totpKey);
        TOTPGenerator totpGenerator = new TOTPGenerator();
        var totp = totpGenerator.generateCurrent(totpSecret);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://kite.trade/connect/login?api_key="+apiKey+"&v=3");
            page.locator("#userid").fill(userId);
            page.locator("#password").fill(password);
            page.locator("#container > div > div > div.login-form > form > div.actions > button").click();

            if(page.locator("xpath=//*[@id=\"userid\"]").isVisible()){
//                todo: find and verify a selector
                page.locator("xpath=//*[@id=\"userid\"]").fill(totp.getValue());
            }
            page.waitForURL("**");
            System.out.println(page.url());
            return page.url();
        }
    }
}
