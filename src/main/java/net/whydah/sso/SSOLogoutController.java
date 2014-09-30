package net.whydah.sso;

import net.whydah.sso.config.AppConfig;
import net.whydah.sso.util.SSOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;

@Controller
public class SSOLogoutController {
    SSOHelper sso = new SSOHelper();
    private final static Logger logger = LoggerFactory.getLogger(SSOLogoutController.class);

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Model model) {
        String redirectURI = request.getParameter("redirectURI");
        String LOGOURL="/sso/images/site-logo.png";
        try {
            Properties properties = AppConfig.readProperties();
            LOGOURL = properties.getProperty("logourl");

        } catch (Exception e){

        }
        model.addAttribute("logoURL", LOGOURL);
        if (redirectURI != null && redirectURI.length() > 3) {
            model.addAttribute("redirect", redirectURI);
        } else {
            model.addAttribute("redirect", "welcome");
        }

        String usertoken = request.getParameter(SSOHelper.USER_TOKEN_REFERENCE_NAME);
        if (usertoken != null && usertoken.length() > 3) {
            model.addAttribute("TokenID", usertoken);
            return "logout";
        } else {
            return "action";
        }

    }

    @RequestMapping("/logoutaction")
    public String logoutAction(HttpServletRequest request, HttpServletResponse response, Model model) {

        //model.
        String usertokenid = request.getParameter(SSOHelper.USER_TOKEN_REFERENCE_NAME);
        String redirectURI = request.getParameter("redirectURI");

        if (usertokenid != null && usertokenid.length() > 1) {
            logger.info("logoutAction - releasing usertokenid={}",usertokenid);
            sso.releaseUserToken(usertokenid);
        }
        String usertokenidfromcookie =sso.getUserTokenIdFromCookie(request,response).getUsertokenid();
        logger.info("logoutAction - releasing usertokenid={} found in cookie",usertokenidfromcookie);
        sso.releaseUserToken(usertokenidfromcookie);

        clearAllWhydahCookies(request, response);

        String LOGOURL="/sso/images/site-logo.png";
        try {
            Properties properties = AppConfig.readProperties();
            LOGOURL = properties.getProperty("logourl");

        } catch (Exception e){

        }
        model.addAttribute("logoURL", LOGOURL);

        model.addAttribute("redirect", redirectURI);
        return "action";
    }

    private void clearAllWhydahCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            logger.trace("clearAllWhydahCookies - Found {} cookie(s)", cookies.length);
            for (Cookie cookie : cookies) {
                logger.trace("clearAllWhydahCookies - Checking cookie:" + cookie.getName());
                if (!SSOHelper.USER_TOKEN_REFERENCE_NAME.equals(cookie.getName())) {
                    continue;
                }


                String usertokenid = cookie.getValue();
                sso.releaseUserToken(usertokenid);
                logger.trace("clearAllWhydahCookies - releaseUserToken  usertokenid: {}  ",usertokenid);
                cookie.setValue("logout");
                response.addCookie(cookie);
                logger.trace("clearAllWhydahCookies - Reset cookie.  usertokenid: {}  Cookie: {}",usertokenid, cookie);
            }
        }
    }

}
