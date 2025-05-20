/*
1. В зависимости от варианта выполняется разработка web-приложения, т.е. сервлета.
Примеры приведены выше.
2. Для создания web-приложения в NetBeans IDE нужно выбрать в меню пункт
«Файл\Создать проект», в появившемся диалоговом окне выбрать категорию «Java
Web» и соответствующий этой категории тип проекта «Веб-приложение». Нажать
«Далее» и в появившейся вкладке «Имя и расположение» ввести название проекта.
Снова нажать «Далее» в появившейся вкладке «Сервер и параметры настройки»
указать сервер «Tomcat» (либо GlassFish) и версию «Java EE 7 web». Также на этой
вкладке может быть изменён «контекстный путь». Нажать «Готово».
3. Следующий шаг - создание сервлета в web-приложении. Для этого нужно вызвать
контекстное меню при нажатии правой клавиши мыши на имени проекта вашего
web-приложения (которое обычно располагается слева, в окне «Проекты"). В этом
меню нужно выбрать «Новый\Сервлет…». На вкладке «Имя и расположение» задать
имя класса сервлета. Нажать «Готово».
4. Необходимо обеспечить передачу параметров в сервлет через строку url-запроса.
5. При перезагрузке страницы сервлета должно отображаться: значение счётчика обращений к странице
сервлета после его запуска.
6. Организовать вывод результатов работы сервлета: данные полученные от сервлета должны быть каким-то образом
размещены в видимой таблице, в таблице допускается произвольное число столбцов
и строк.
7. Реализовать при обновлении страницы сервлета: увеличение размера текста в таблице до заданной величины,
после чего на странице должна появляться надпись (не в таблице), информирующая о том, что дальнейшее
увеличение не возможно.
8. Реализовать возможность сброса размера текста в таблице через параметр строки url запроса: до указанного значения.
9. Среди параметров, передаваемых в сервлет, нужно передавать Ф.И.О студента,
выполнившего разработку сервлета, и номер его группы, которые должны
отображаться следующим образом: 0 - в заголовке web-страницы возвращаемой
сервлетом клиенту.
10. Взять за основную функцию, которую вычисляет сервлет, реализованную в первой
лабораторной работе.
11. При необходимости могут быть изменены порты, по которым контейнер сервлетов
Tomcat слушает запросы. Для изменения портов нужно в среде NetBeans войти в
меню «Сервис\Серверы»: оставить порт по умолчанию.
111100
*/
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.laba_7_net2;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


//ProcessArray?name=Громов%20Даниил%20Константинович&group=4317&b=5&sequence=1,3,6,8,10
/**
 *
 * @author danii
 */
@WebServlet(name = "ProcessArray", urlPatterns = {"/ProcessArray"})
public class ProcessArray extends HttpServlet {
    static int counter = 0;
    static double fontSize = 1;
    static final int MAX_FONT_SIZE = 5;
    static boolean maxFontReached = false;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
         // Увеличиваем счетчик обращений
        counter++;
        
        // Получаем параметры из URL
        String fullName = request.getParameter("name");
        String group = request.getParameter("group");
        String bParam = request.getParameter("b");
        String sequenceParam = request.getParameter("sequence");
        
        // Обработка параметра сброса размера шрифта
        String resetFont = request.getParameter("resetFont");
        if (resetFont != null && !resetFont.isEmpty()) {
            try {
                fontSize = Double.parseDouble(resetFont);
                maxFontReached = false;
            } catch (NumberFormatException e) {
                fontSize = 1;
            }
        } else if (fontSize < MAX_FONT_SIZE) {
            fontSize += 0.5;
        } else {
            maxFontReached = true;
        }
        
        // Выполняем основную функцию из лабораторной работы 1
        String sequenceResult = "";
        if (bParam != null && sequenceParam != null) {
            try {
                int b = Integer.parseInt(bParam);
                String[] seqParts = sequenceParam.split(",");
                int[] sequence = new int[seqParts.length];
                
                for (int i = 0; i < seqParts.length; i++) {
                    sequence[i] = Integer.parseInt(seqParts[i].trim());
                }
                
                if (isSorted(sequence)) {
                    int[] result = insertAndSort(sequence, b);
                    for (int num : result) {
                        sequenceResult += num + " ";
                    }
                } else {
                    sequenceResult = "Ошибка: последовательность должна быть неубывающей";
                }
            } catch (NumberFormatException e) {
                sequenceResult = "Ошибка в формате параметров";
            }
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>" + (fullName != null ? fullName : "ФИО") + ", " + (group != null ? group : "Группа") + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Лабораторная работа №7. Сервлеты</h1>");
            
            // Выводим счетчик обращений
            out.println("<p>Количество обращений к сервлету: " + counter + "</p>");
            
            // Выводим предупреждение о максимальном размере шрифта
            if (maxFontReached) {
                out.println("<p style='color:red;'>Достигнут максимальный размер шрифта!</p>");
            }
            
            // Выводим таблицу с результатами
            out.println("<table border='1' style='font-size:" + fontSize + "em;'>");
            out.println("<tr><th>Параметр</th><th>Значение</th></tr>");
            out.println("<tr><td>ФИО</td><td>" + (fullName != null ? fullName : "Не указано") + "</td></tr>");
            out.println("<tr><td>Группа</td><td>" + (group != null ? group : "Не указана") + "</td></tr>");
            out.println("<tr><td>Число b</td><td>" + (bParam != null ? bParam : "Не указано") + "</td></tr>");
            out.println("<tr><td>Последовательность</td><td>" + (sequenceParam != null ? sequenceParam : "Не указана") + "</td></tr>");
            out.println("<tr><td>Результат</td><td>" + sequenceResult + "</td></tr>");
            out.println("</table>");
            
            // Ссылка для сброса размера шрифта
            out.println("<p><a href='ProcessArray?resetFont=1'>Сбросить размер шрифта</a></p>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    // Методы из первой лабораторной работы
    public static int[] insertAndSort(int[] sequence, int b) {
        int[] result = new int[sequence.length + 1];
        int i = 0;
        int j = 0;

        while (i < sequence.length && sequence[i] <= b) {
            result[j] = sequence[i];
            i++;
            j++;
        }

        result[j] = b;
        j++;

        while (i < sequence.length) {
            result[j] = sequence[i];
            i++;
            j++;
        }

        return result;
    }

    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }
}