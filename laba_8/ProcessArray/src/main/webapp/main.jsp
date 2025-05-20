<%-- 
    Document   : main
    Created on : 19 мая 2025 г., 15:46:32
    Author     : danii
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Два</title>
    </head>
    <body>
        <jsp:useBean id="seqHandler" scope="session" class="com.mycompany.processarray.SequenceHandler" />
        
        <h1>Введите данные</h1>
        
        <%-- Показываем триггер ТОЛЬКО если это не первый переход --%>
        <% if (!seqHandler.isFirstVisit()) { %>
            <p>Состояние триггера: ${seqHandler.triggerStatus}</p>
        <% } %>
        
        <form action="result.jsp" method="post">
            <p>Введите последовательность чисел (через пробел):</p>
            <input type="text" name="sequence" value="${seqHandler.sequence}" />
            
            <p>Введите число для вставки:</p>
            <input type="text" name="number" value="${seqHandler.number}" />
            
            <br/><br/>
            <input type="submit" value="Обработать" />
        </form>
    </body>
</html>
