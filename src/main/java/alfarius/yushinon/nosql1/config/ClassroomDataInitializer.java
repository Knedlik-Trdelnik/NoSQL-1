package alfarius.yushinon.nosql1.config;

import alfarius.yushinon.nosql1.database.repositories.ClassroomRepository;
import alfarius.yushinon.nosql1.database.repositories.TimeWindowRepository;
import alfarius.yushinon.nosql1.entity.Classroom;
import alfarius.yushinon.nosql1.entity.TimeWindow;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClassroomDataInitializer implements CommandLineRunner {

    private final ClassroomRepository classroomRepository;
    private final TimeWindowRepository timeWindowRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (classroomRepository.count() == 0) {
            List<Classroom> classrooms = List.of(
                    createClassroom("Аудитория 101 — Большая лекционная"),
                    createClassroom("Аудитория 102 — Малая лекционная"),
                    createClassroom("Компьютерный класс 201 (Java/Spring)"),
                    createClassroom("Компьютерный класс 202 (Python/AI)"),
                    createClassroom("Зал семинаров 301"),
                    createClassroom("Зал семинаров 302"),
                    createClassroom("Лаборатория робототехники 401"),
                    createClassroom("Лаборатория кибербезопасности 402"),
                    createClassroom("Коворкинг-пространство 501"),
                    createClassroom("Конференц-зал 502 (Учёный совет)")
            );
            classroomRepository.saveAll(classrooms);
            System.out.println("✦ Успешно создано 10 аудиторий в PostgreSQL!");
        }

        if (timeWindowRepository.count() == 0) {
            List<TimeWindow> timeWindows = List.of(
                    createTimeWindow(8, 30, 10, 0),
                    createTimeWindow(10, 15, 11, 45),

                    createTimeWindow(12, 15, 13, 45),
                    createTimeWindow(14, 0, 15, 30),
                    createTimeWindow(15, 45, 17, 15),

                    createTimeWindow(17, 30, 19, 0),
                    createTimeWindow(19, 15, 20, 45)
            );
            timeWindowRepository.saveAll(timeWindows);
            System.out.println("✦ Успешно созданы временные слоты с 8:30 до 20:45!");
        }
    }

    private Classroom createClassroom(String name) {
        Classroom c = new Classroom();
        c.setName(name);
        return c;
    }

    private TimeWindow createTimeWindow(int startHour, int startMin, int endHour, int endMin) {
        TimeWindow tw = new TimeWindow();
        LocalTime start = LocalTime.of(startHour, startMin);
        LocalTime end = LocalTime.of(endHour, endMin);
        tw.setTimeStart(start);
        tw.setTimeEnd(end);
        tw.setTimeDuration((int) Duration.between(start, end).toMinutes());
        return tw;
    }

    @PostConstruct
    public void init() {
        try {
            System.out.println(">>> [INIT] Принудительное наполнение справочников...");

            if (classroomRepository.count() == 0) {
                List<Classroom> classrooms = List.of(
                        createClassroom("Аудитория 101 — Большая лекционная"),
                        createClassroom("Аудитория 102 — Малая лекционная"),
                        createClassroom("Компьютерный класс 201 (Java/Spring)"),
                        createClassroom("Компьютерный класс 202 (Python/AI)"),
                        createClassroom("Зал семинаров 301"),
                        createClassroom("Зал семинаров 302"),
                        createClassroom("Лаборатория робототехники 401"),
                        createClassroom("Лаборатория кибербезопасности 402"),
                        createClassroom("Коворкинг-пространство 501"),
                        createClassroom("Конференц-зал 502 (Учёный совет)")
                );
                classroomRepository.saveAll(classrooms);
                System.out.println(">>> [INIT] Успешно создано 10 аудиторий!");
            }

            if (timeWindowRepository.count() == 0) {
                List<TimeWindow> timeWindows = List.of(
                        createTimeWindow(8, 30, 10, 0),
                        createTimeWindow(10, 15, 11, 45),
                        createTimeWindow(12, 15, 13, 45),
                        createTimeWindow(14, 0, 15, 30),
                        createTimeWindow(15, 45, 17, 15),
                        createTimeWindow(17, 30, 19, 0),
                        createTimeWindow(19, 15, 20, 45)
                );
                timeWindowRepository.saveAll(timeWindows);
                System.out.println(">>> [INIT] Успешно созданы временные слоты!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}