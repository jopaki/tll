package com.tll.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jpk
 * @since Aug 25, 2012
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final long REMEMBER_ME_DURATION = 1000 * 60 * 60 * 24; // 1 DAY
	
	private String username = "utest", password = "ptest";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = req.getRequestURI();

		// simulate latency giving client a change to test glass panel etc..
		if(!"/Login/app".equals(path)) {
			try { Thread.sleep(1500); } catch(InterruptedException e) { throw new Error(e); }
		}

		if("/Login/login".equals(path)) {
			// login
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			boolean ok;
			synchronized(this) {
				ok = (this.username.equals(username) && this.password.equals(password));
			}
			if(ok) {
				// success
				if("1".equals(req.getParameter("rememberMe"))) {
					// set remember me cookie
					String sessionID = req.getSession().getId();
			    Cookie cookie = new Cookie("sid", sessionID);
			    cookie.setMaxAge((int) REMEMBER_ME_DURATION);
			    resp.addCookie(cookie);
				}
		    // redirect to the app
		    resp.sendRedirect("/Login/app");
			} else {
				// fail
				resp.sendError(401, "Invalid login credentials.");
			}
		} else if("/Login/pr".equals(path)) {
			// check if email in system
			String email = req.getParameter("email");
			if(!"test@dev.com".equals(email)) {
				resp.sendError(401, "The given email address is not recognized.  Make sure you typed it correctly.");
			}
			// reset password
			synchronized(this) {
				this.password = "reset";
				resp.setStatus(200);
			}
		} else if("/Login/snp".equals(path)) {
			// submit new password
			// change password to what user specified
			String password = req.getParameter("password");
			String cpassword = req.getParameter("cpassword");
			if(password == null || !password.equals(cpassword)) {
				// passwords don't match
				resp.sendError(401, "Password and confirm passwords do not match");
			} else {
				synchronized(this) {
					this.password = password;
				}
				resp.setStatus(200);
			}
		// test app endpoint
		} else if("/Login/app".equals(path)) {
			resp.setStatus(200);
			String s = "Login successful and stuff!";
			resp.getWriter().append(s);
			resp.getWriter().close();
		} else {
			resp.sendError(404, "Unhandled request.");
		}
		
	}

}
