package com.acikartirma.acikartirma.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "SecurityFilter", urlPatterns = {"*.xhtml"})
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String loginURL = req.getContextPath() + "/login.xhtml";
        String registerURL = req.getContextPath() + "/register.xhtml";
        String indexURL = req.getContextPath() + "/index.xhtml";
        String reqURI = req.getRequestURI();

        // Senin projendeki oturum değişkeni "valid_user" olduğu için onu kullanıyoruz
        boolean isLoggedIn = (session != null && session.getAttribute("valid_user") != null);
        boolean isLoginRequest = reqURI.equals(loginURL);
        boolean isRegisterRequest = reqURI.equals(registerURL);
        boolean isResourceRequest = reqURI.startsWith(req.getContextPath() + "/jakarta.faces.resource");

        if (isLoggedIn) {
            // DURUM 1: Kullanıcı giriş yapmış.
            // Eğer hala login veya register sayfasına gitmeye çalışıyorsa ana sayfaya fırlat
            if (isLoginRequest || isRegisterRequest) {
                res.sendRedirect(indexURL);
            } else {
                chain.doFilter(request, response);
            }
        } else if (isLoginRequest || isRegisterRequest || isResourceRequest) {
            // DURUM 2: Giriş yapmamış ama izinli sayfalara (login, register, css/js) gidiyor.
            chain.doFilter(request, response);
        } else {
            // DURUM 3: Giriş yapmamış ve yasaklı bir yere girmeye çalışıyor.

            // HOCANIN AJAX KONTROLÜ: Eğer bu bir AJAX isteğiyse özel XML gönder
            if (isAJAXRequest(req)) {
                res.setContentType("text/xml");
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write("<?xml version='1.0' encoding='UTF-8'?>"
                        + "<partial-response><redirect url='" + loginURL + "'/></partial-response>");
            } else {
                // Normal bir sayfa isteğiyse düz redirect yap
                res.sendRedirect(loginURL);
            }
        }
    }

    // Hocanın projesinden aldığımız kritik AJAX kontrol metodu
    private boolean isAJAXRequest(HttpServletRequest request) {
        String facesRequest = request.getHeader("Faces-Request");
        return "partial/ajax".equals(facesRequest);
    }
}