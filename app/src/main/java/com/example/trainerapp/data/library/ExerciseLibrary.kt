package com.example.trainerapp.data.library

data class LibraryExercise(val name: String)

data class ExerciseCategory(val title: String, val exercises: List<LibraryExercise>)

interface ExerciseLibrary {
    val id: String
    val title: String
    val categories: List<ExerciseCategory>
}

private fun cat(title: String, vararg names: String) =
    ExerciseCategory(title, names.map { LibraryExercise(it) })

object CrossfitLibrary : ExerciseLibrary {
    override val id = "crossfit"
    override val title = "Кроссфит"
    override val categories = listOf(
        cat(
            "Приседания и выпады",
            "Приседание без веса (Air Squat)",
            "Фронтальный присед (Front Squat, FS)",
            "Присед со штангой на спине (Back Squat, BS)",
            "Присед над головой (Overhead Squat, OHS)",
            "Присед Зерчера (Zercher Squat)",
            "Приседание-пистолет (Pistol Squat)",
            "Выпад (Lunge)",
            "Выпад с ходьбой (Walking Lunge)",
            "Выпад со штангой на груди (Front Rack Lunge)",
            "Выпад с гантелью у груди (Front-Rack Dumbbell Lunge)",
            "Выпад с гантелью над головой (Overhead Dumbbell Lunge)",
            "Выпад с гантелями над головой во время ходьбы (Overhead Dumbbell Walking Lunge)"
        ),
        cat(
            "Тяги, жимы и силовые движения со штангой",
            "Становая тяга (Deadlift, DL)",
            "Сумо-тяга (Sumo Deadlift)",
            "Сумо-тяга к подбородку (Sumo Deadlift High Pull, SDHP)",
            "Гуд-морнинг (Good Morning)",
            "Жим стоя (Shoulder Press, SP)",
            "Швунг (Push Press, PP)",
            "Толчковый швунг (Push Jerk, PJ)",
            "Жим лёжа (Bench Press)",
            "Жим Сотса (Sots Press)",
            "Трастер (Thruster)"
        ),
        cat(
            "Тяжёлая атлетика",
            "Взятие на грудь (Clean)",
            "Силовое взятие (Power Clean)",
            "Взятие с виса (Hang Clean)",
            "Силовое взятие с виса (Hang Power Clean)",
            "Взятие на грудь и толчок (Clean and Jerk, C&J)",
            "Взятие и швунг-толчок (Clean and Push Jerk)",
            "Рывок (Snatch)",
            "Силовой рывок (Power Snatch)",
            "Рывок с виса (Hang Snatch)",
            "Силовой рывок с виса (Hang Power Snatch)",
            "Мышечный рывок (Muscle Snatch)",
            "Приседание в рывке (Snatch Squat)",
            "Рывковый баланс (Snatch Balance)",
            "Толчок в разножку (Split Jerk)",
            "Взятие в разножку (Split Clean)",
            "Рывок в разножку (Split Snatch)"
        ),
        cat(
            "Перекладина и кольца",
            "Подтягивание (Pull-Up)",
            "Подтягивание с киппингом (Kipping Pull-Up)",
            "Подтягивание баттерфляем (Butterfly Pull-Up)",
            "Подтягивание грудью к перекладине (Chest-to-Bar Pull-Up, C2B)",
            "L-подтягивание (L-Pull-Up)",
            "Подъём ног к перекладине (Toes-to-Bar, TTB)",
            "Строгий подъём ног к перекладине (Strict Toes-to-Bar)",
            "Подъём коленей к локтям (Knees-to-Elbows, K2E)",
            "Подъём ног к кольцам (Toes-to-Rings)",
            "Выход силой на перекладине (Bar Muscle-Up, BMU)",
            "Выход силой на кольцах (Ring Muscle-Up, RMU)",
            "Строгий выход силой на перекладине (Strict Bar Muscle-Up)",
            "Выход силой на перекладине с киппингом (Kipping Bar Muscle-Up)",
            "Строгий выход силой на кольцах (Strict Ring Muscle-Up)",
            "Выход силой на кольцах с киппингом (Kipping Ring Muscle-Up)",
            "Тяга к кольцам (Ring Row)",
            "Отжимание на кольцах (Ring Dip)",
            "Вращение «skin the cat» на кольцах (Skin the Cat)"
        ),
        cat(
            "Стойки на руках",
            "Стойка на руках (Handstand)",
            "Стойка на руках у стены (Wall Handstand)",
            "Отжимание в стойке на руках (Handstand Push-Up, HSPU)",
            "Строгое отжимание в стойке на руках (Strict HSPU)",
            "Отжимание в стойке на руках с киппингом (Kipping HSPU)",
            "Отжимание в стойке на руках с дефицитом (Deficit HSPU)",
            "Ходьба на руках (Handstand Walk, HSW)",
            "Пируэт в стойке на руках (Handstand Pirouette)",
            "Выход в стойку на руках из широкой стойки (Straddle Press to Handstand)",
            "Подъём по стене в стойку (Wall Walk)"
        ),
        cat(
            "Отжимания, упоры и другие движения",
            "Отжимание (Push-Up)",
            "Отжимание на брусьях (Dip)",
            "Свободный L-sit (L-Sit)",
            "L-sit на кольцах (Ring L-Sit)",
            "L-sit в висе (Hanging L-Sit)",
            "Передний упор на руках (Front Scale)",
            "Задний упор на руках (Back Scale)",
            "Подъём по канату (Rope Climb)",
            "Подъём по канату без помощи ног (Legless Rope Climb)",
            "Подъём по канату в положении L (L-Sit Rope Climb)",
            "Переворот через перекладину (Pull-Over)",
            "Прострел между упорами (Shoot-Through)",
            "Переворот назад в упор (Back Roll to Support)",
            "Перевёрнутое бёрпи (Inverted Burpee)",
            "Подъём и разгибание на GHD (GHD Hip Extension)",
            "Подъём корпуса на GHD (GHD Sit-Up)",
            "Скручивание на AbMat (AbMat Sit-Up)"
        ),
        cat(
            "Гантели и гири",
            "Взятие гантели (Dumbbell Clean)",
            "Взятие гантели с виса (Hang Dumbbell Clean)",
            "Силовое взятие гантели (Dumbbell Power Clean)",
            "Рывок гантели (Dumbbell Snatch)",
            "Силовой рывок гантели (Dumbbell Power Snatch)",
            "Взятие гантели в присед (Dumbbell Squat Clean)",
            "Присед с гантелями (Dumbbell Squat)",
            "Приседание с гантелью над головой (Dumbbell Overhead Squat)",
            "Жим гантелей (Dumbbell Press)",
            "Швунг с гантелями (Dumbbell Push Press)",
            "Толчковый швунг с гантелями (Dumbbell Push Jerk)",
            "Трастер с гантелями (Dumbbell Thruster)",
            "Перенос гантелей в руках (Farmer’s Carry)",
            "Рывок гири (Kettlebell Snatch)",
            "Мах гирей (Kettlebell Swing)",
            "Турецкий подъём (Turkish Get-Up)"
        ),
        cat(
            "Мячи, прыжки и кондиционные упражнения",
            "Взятие мяча-медбола (Medicine-Ball Clean)",
            "Подъём мяча-медбола (Medicine-Ball Clean)",
            "Бросок мяча в стену (Wall Ball)",
            "Бросок мяча о пол (Slam Ball)",
            "Бёрпи (Burpee)",
            "Бёрпи через коробку (Burpee Box Jump)",
            "Прыжок на коробку (Box Jump)",
            "Зашагивание на коробку (Box Step-Up)",
            "Одинарные прыжки через скакалку (Single-Unders, SU)",
            "Двойные прыжки через скакалку (Double-Unders, DU)",
            "Гребля на тренажёре (Row)",
            "Езда на велотренажёре (Bike)",
            "Лыжный тренажёр (SkiErg)",
            "Бег (Run)",
            "Плавание (Swim)"
        )
    )
}

object BodybuildingLibrary : ExerciseLibrary {
    override val id = "bodybuilding"
    override val title = "Бодибилдинг"
    override val categories = listOf(
        cat(
            "Грудь",
            "Жим штанги лёжа (Barbell Bench Press)",
            "Жим штанги на наклонной скамье (Incline Barbell Bench Press)",
            "Жим штанги на скамье с отрицательным наклоном (Decline Barbell Bench Press)",
            "Жим гантелей лёжа (Dumbbell Bench Press)",
            "Жим гантелей на наклонной скамье (Incline Dumbbell Press)",
            "Разведение гантелей лёжа (Dumbbell Fly)",
            "Разведение гантелей на наклонной скамье (Incline Dumbbell Fly)",
            "Сведение рук в кроссовере (Cable Crossover)",
            "Сведение рук в тренажёре (Pec Deck Fly)",
            "Отжимания на брусьях с наклоном (Chest Dip)",
            "Отжимания от пола (Push-Up)"
        ),
        cat(
            "Спина",
            "Тяга штанги в наклоне (Barbell Row)",
            "Тяга гантели одной рукой (One-Arm Dumbbell Row)",
            "Тяга Т-грифа (T-Bar Row)",
            "Тяга нижнего блока к животу (Seated Cable Row)",
            "Тяга верхнего блока к груди (Lat Pulldown)",
            "Тяга верхнего блока обратным хватом (Reverse-Grip Lat Pulldown)",
            "Подтягивания широким хватом (Wide-Grip Pull-Up)",
            "Тяга прямыми руками на блоке (Straight-Arm Pulldown)",
            "Становая тяга (Deadlift)",
            "Гиперэкстензия (Back Extension)",
            "Шраги со штангой (Barbell Shrug)",
            "Шраги с гантелями (Dumbbell Shrug)"
        ),
        cat(
            "Плечи",
            "Жим штанги над головой (Overhead Press)",
            "Жим гантелей сидя (Seated Dumbbell Press)",
            "Жим в тренажёре (Machine Shoulder Press)",
            "Подъём гантелей в стороны (Dumbbell Lateral Raise)",
            "Подъём гантелей перед собой (Dumbbell Front Raise)",
            "Разведение гантелей в наклоне (Bent-Over Dumbbell Lateral Raise)",
            "Обратная бабочка (Reverse Pec Deck)",
            "Тяга штанги к подбородку (Upright Row)",
            "Тяга каната к лицу (Face Pull)"
        ),
        cat(
            "Бицепс",
            "Подъём штанги на бицепс (Barbell Curl)",
            "Подъём EZ-штанги на бицепс (EZ-Bar Curl)",
            "Подъём гантелей на бицепс (Dumbbell Curl)",
            "Попеременный подъём гантелей (Alternating Dumbbell Curl)",
            "Молотковые сгибания (Hammer Curl)",
            "Сгибание рук на скамье Скотта (Preacher Curl)",
            "Сгибание рук на наклонной скамье (Incline Dumbbell Curl)",
            "Сгибание рук на нижнем блоке (Cable Curl)",
            "Концентрированное сгибание на бицепс (Concentration Curl)"
        ),
        cat(
            "Трицепс",
            "Французский жим лёжа (Lying Triceps Extension)",
            "Разгибание рук со штангой из-за головы (Overhead Triceps Extension)",
            "Разгибание рук на верхнем блоке (Triceps Pushdown)",
            "Разгибание рук с канатной рукоятью (Rope Pushdown)",
            "Разгибание руки с гантелью из-за головы (Dumbbell Overhead Extension)",
            "Разгибание руки с гантелью в наклоне (Dumbbell Kickback)",
            "Жим штанги узким хватом (Close-Grip Bench Press)",
            "Отжимания на брусьях на трицепс (Triceps Dip)"
        ),
        cat(
            "Предплечья",
            "Сгибание кистей со штангой (Barbell Wrist Curl)",
            "Разгибание кистей со штангой (Reverse Wrist Curl)",
            "Сгибание кистей с гантелями (Dumbbell Wrist Curl)",
            "Обратное сгибание рук со штангой (Reverse Curl)",
            "Удержание блинов пальцами (Plate Pinch Hold)"
        ),
        cat(
            "Квадрицепсы и ягодицы",
            "Приседание со штангой (Barbell Squat)",
            "Фронтальный присед (Front Squat)",
            "Гакк-присед (Hack Squat)",
            "Жим ногами (Leg Press)",
            "Разгибание ног в тренажёре (Leg Extension)",
            "Болгарский сплит-присед (Bulgarian Split Squat)",
            "Выпады с гантелями (Dumbbell Lunge)",
            "Ягодичный мост со штангой (Barbell Hip Thrust)",
            "Отведение ноги назад в кроссовере (Cable Glute Kickback)",
            "Отведение бедра в тренажёре (Hip Abduction)"
        ),
        cat(
            "Бицепс бедра и ягодицы",
            "Румынская становая тяга (Romanian Deadlift)",
            "Становая тяга на прямых ногах (Stiff-Leg Deadlift)",
            "Сгибание ног лёжа (Lying Leg Curl)",
            "Сгибание ног сидя (Seated Leg Curl)",
            "Сгибание ноги стоя (Standing Leg Curl)",
            "Наклоны со штангой на плечах (Good Morning)",
            "Гиперэкстензия с акцентом на ягодицы (Glute-Focused Back Extension)"
        ),
        cat(
            "Икры",
            "Подъём на носки стоя (Standing Calf Raise)",
            "Подъём на носки сидя (Seated Calf Raise)",
            "Подъём на носки в жиме ногами (Leg Press Calf Raise)",
            "Подъём на носок на одной ноге (Single-Leg Calf Raise)"
        ),
        cat(
            "Пресс и корпус",
            "Скручивания (Crunch)",
            "Скручивания на блоке (Cable Crunch)",
            "Подъём ног в висе (Hanging Leg Raise)",
            "Подъём коленей в висе (Hanging Knee Raise)",
            "Обратные скручивания (Reverse Crunch)",
            "Русские скручивания (Russian Twist)",
            "Планка (Plank)",
            "Боковая планка (Side Plank)",
            "Колесо для пресса (Ab Wheel Rollout)",
            "«Дровосек» на блоке (Cable Woodchop)"
        )
    )
}

object PrehabLibrary : ExerciseLibrary {
    override val id = "prehab"
    override val title = "Подготовка и мобильность"
    override val categories = listOf(
        cat(
            "Разминка · Плечи, лопатки и грудной отдел",
            "Провороты рук с палкой или резиной (Shoulder Pass-Through)",
            "Разведения резины перед собой (Band Pull-Apart)",
            "Внешняя ротация плеча с резиной (Band External Rotation)",
            "Круги руками (Arm Circles)",
            "Вращения плечами (Shoulder Rolls)",
            "Повороты грудного отдела на четвереньках (Thoracic Rotation)",
            "Кошка-корова (Cat-Cow)"
        ),
        cat(
            "Разминка · Общая динамическая",
            "«Червячок» (Inchworm Walk)",
            "Выпад с поворотом корпуса (Lunge with Rotation)",
            "Скорпион лёжа (Scorpion)",
            "Присед с паузой и раскрытием таза (Deep Squat Hold with Rocking)",
            "Махи ногой вперёд-назад (Leg Swings Forward-Back)",
            "Махи ногой в сторону (Leg Swings Side-to-Side)",
            "Ягодичный мост без веса (Glute Bridge)"
        ),
        cat(
            "Разминка · Кор и «холлоу»",
            "Hollow hold (Удержание холлоу)",
            "Hollow rocks (Покачивания в холлоу)",
            "Tuck hollow hold (Холлоу с согнутыми коленями)",
            "Dead bug (Мёртвый жук)",
            "Планка на предплечьях (Forearm Plank)"
        ),
        cat(
            "Силовые · Плечи и верх спины",
            "L-raise (Подъём рук буквой L)",
            "U-raise (Подъём предплечий из позиции L)",
            "Y-raise (Подъём рук по диагонали буквой Y)",
            "Face pull с резиной (Band Face Pull)",
            "Scapular push-up (Отжимание лопатками)",
            "Подъём рук Y лёжа на животе (Prone Y-Raise)",
            "Подъём рук T лёжа на животе (Prone T-Raise)",
            "Подъём рук W лёжа на животе (Prone W-Raise)"
        ),
        cat(
            "Силовые · Задняя поверхность бедра и ягодицы",
            "Nordic hamstring (Нордик)",
            "Сгибание ног с полотенцами или слайдерами (Slider Hamstring Curl)",
            "Румынская тяга без веса (Bodyweight Romanian Deadlift)",
            "Ягодичный мост на одной ноге (Single-Leg Glute Bridge)",
            "Hip thrust без веса (Bodyweight Hip Thrust)",
            "Одноногая румынская тяга без веса (Bodyweight Single-Leg RDL)"
        )
    )
}

data class LibraryEntry(val libraryTitle: String, val categoryTitle: String, val exerciseName: String)

object ExerciseLibraryRegistry {
    // Новые библиотеки добавляются одной строкой сюда — весь остальной код уже готов к ним
    val libraries: List<ExerciseLibrary> = listOf(CrossfitLibrary, BodybuildingLibrary, PrehabLibrary)

    fun allEntries(): List<LibraryEntry> = libraries.flatMap { lib ->
        lib.categories.flatMap { category ->
            category.exercises.map { LibraryEntry(lib.title, category.title, it.name) }
        }
    }

    fun search(query: String): List<LibraryEntry> {
        val q = query.trim()
        val all = allEntries()
        return if (q.isEmpty()) all
        else all.filter {
            it.exerciseName.contains(q, ignoreCase = true) ||
                it.categoryTitle.contains(q, ignoreCase = true) ||
                it.libraryTitle.contains(q, ignoreCase = true)
        }
    }
}
