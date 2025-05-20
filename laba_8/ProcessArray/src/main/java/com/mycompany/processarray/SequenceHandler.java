/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.processarray;

/**
 *
 * @author danii
 */
public class SequenceHandler {
    private String sequence;
    private String number;
    private String result;
    private boolean trigger;
    private boolean firstVisit;

    public SequenceHandler() {
        sequence = "";
        number = "";
        result = "";
        trigger = false;
        firstVisit = true;
    }

    // Геттеры и сеттеры
    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
    
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public boolean isTrigger() {
        return trigger;
    }
    public void flipTrigger() {
        this.trigger = !this.trigger; // Переключаем состояние
    }
    public String getTriggerStatus() {
        return trigger ? "активен" : "неактивен";
    }
    
    public boolean isFirstVisit() {
        return firstVisit;
    }
    
     public void markAsNotFirstVisit() {
        this.firstVisit = false;
    }
    
    public void reset() {
    this.sequence = "";
    this.number = "";
    this.result = "";
    this.trigger = false;
    this.firstVisit = true;
}

    // Метод для обработки последовательности
    public void processSequence() {
        try {
            int b = Integer.parseInt(number.trim());
            String[] parts = sequence.split("\\s+");
            int[] arr = new int[parts.length];
            
            for (int i = 0; i < parts.length; i++) {
                arr[i] = Integer.parseInt(parts[i].trim());
            }
            
            if (!isSorted(arr)) {
                result = "Ошибка: Последовательность должна быть неубывающей";
                return;
            }
            
            int[] res = insertAndSort(arr, b);
            StringBuilder sb = new StringBuilder();
            for (int num : res) {
                sb.append(num).append(" ");
            }
            result = sb.toString().trim();
            
        } catch (NumberFormatException e) {
            result = "Ошибка: Неверный формат чисел";
        }
    }

    private boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }

    private int[] insertAndSort(int[] sequence, int b) {
        int[] result = new int[sequence.length + 1];
        int i = 0, j = 0;
        
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
}