package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);
        System.out.println("-----------------------------------------------");
        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        if (meals == null || meals.isEmpty()) return new ArrayList<>();
        Map<Boolean, List<UserMealWithExcess>> result = crateEmptyMap();
        int allCalories = 0;
        for (UserMeal meal : meals) {
            if (checkTime(meal, startTime, endTime)) {
                fillMap(result, meal);
                allCalories += meal.getCalories();
            }
        }
        return result.get(allCalories > caloriesPerDay);
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<Boolean, List<UserMealWithExcess>> result = crateEmptyMap();
        return ofNullable(meals)
                .filter(m -> !m.isEmpty())
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .filter(meal -> checkTime(meal, startTime, endTime))
                .peek(meal -> fillMap(result, meal))
                .map(UserMeal::getCalories)
                .reduce(Integer::sum)
                .map(allCalories -> result.get(allCalories > caloriesPerDay))
                .orElse(new ArrayList<>());
    }

    private static Map<Boolean, List<UserMealWithExcess>> crateEmptyMap() {
        Map<Boolean, List<UserMealWithExcess>> result = new HashMap<>();
        result.put(TRUE, new ArrayList<>());
        result.put(FALSE, new ArrayList<>());
        return result;
    }

    private static void fillMap(Map<Boolean, List<UserMealWithExcess>> result, UserMeal meal) {
        UserMealWithExcess userMealWithExcessTrue = new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), TRUE);
        UserMealWithExcess userMealWithExcessFalse = new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), FALSE);
        result.get(TRUE).add(userMealWithExcessTrue);
        result.get(FALSE).add(userMealWithExcessFalse);
    }

    private static boolean checkTime(UserMeal meal, LocalTime startTime, LocalTime endTime) {
        if(startTime == null || endTime == null) throw new RuntimeException("");
        return ofNullable(meal)
                .map(UserMeal::getDateTime)
                .map(LocalDateTime::toLocalTime)
                .filter(time -> time.isAfter(startTime))
                .filter(time -> time.isBefore(endTime))
                .isPresent();
    }

}
