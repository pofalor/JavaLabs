<%-- 
    Document   : result
    Created on : 19 мая 2025 г., 15:47:12
    Author     : danii
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:useBean id="seqHandler" scope="session" class="com.mycompany.processarray.SequenceHandler" />
<% 
seqHandler.flipTrigger();
seqHandler.markAsNotFirstVisit();
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Три</title>
    </head>
    <body>
        <jsp:setProperty name="seqHandler" property="sequence" />
        <jsp:setProperty name="seqHandler" property="number" />
        <% seqHandler.processSequence(); %>
        
        <h1>Результат обработки</h1>
        
        <table border="1">
            <tr>
                <th>Исходная последовательность</th>
                <td>${seqHandler.sequence}</td>
            </tr>
            <tr>
                <th>Число для вставки</th>
                <td>${seqHandler.number}</td>
            </tr>
            <tr>
                <th>Результат</th>
                <td>${seqHandler.result}</td>
            </tr>
        </table>
        
        <br/>
        <form action="main.jsp">
            <input type="submit" value="Вернуться" />
        </form>
    </body>
</html>
