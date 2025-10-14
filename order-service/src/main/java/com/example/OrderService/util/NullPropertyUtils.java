package com.example.OrderService.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * Утилитарный класс для работы с null свойствами объектов.
 * Используется для копирования свойств с исключением null значений.
 */
public class NullPropertyUtils {

    /**
     * Возвращает массив имен свойств со значением null из исходного объекта.
     * Полезно для частичного обновления объектов при копировании свойств.
     *
     * @param source исходный объект
     * @return массив имен свойств со значением null
     */
    public String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }

        return emptyNames.toArray(new String[0]);
    }
} 
