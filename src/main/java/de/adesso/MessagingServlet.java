package de.adesso;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class MessagingServlet extends HttpServlet {

private static final long serialVersionUID = -2435822818866134077L;
	private RabbitTemplate rabbitTemplate;
	private Queue queue;

	public MessagingServlet() {
		super();
		ConnectionFactory connectionFactory = new RabbitServiceCreator(
				new CloudEnvironment()).createSingletonService().service;
		rabbitTemplate = new RabbitTemplate(connectionFactory);
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		queue = new Queue("myQueue", false, true, true);
		rabbitAdmin.declareQueue(queue);
		FanoutExchange exchange = new FanoutExchange("itemUpdateExchange");
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.println("<html><head><title>Get Message</title></head><body>");
		out.println("<center><h2>Get Message</h2></center>");
		out.println("<b>Queue name : </b>" + queue.getName()+"</br>");
		out.println("<br />");
		String update = (String) rabbitTemplate.receiveAndConvert(queue
				.getName());
		if (update == null) {
			out.println("No message, sorry!");
		} else {
			out.println("Message received! <br /> <b>Content: </b><br />");
			out.println(update);
		}
		out.println("</body>");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String message = req.getParameter("message");
		rabbitTemplate.convertAndSend("itemUpdateExchange", "", message);
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		out.println("<html><head><title>Post Message</title></head><body>");
		out.println("<center><h2>Post Message</h2></center>");
		out.println("<br />");
		out.println("Message sent! <br />");
		out.println("<br />");
		out.println("<b>Content: </b><br />");
		out.println(message + "<br />");
		out.println("</body>");
	}

}
