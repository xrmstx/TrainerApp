#!/usr/bin/env python3
import pathlib, sys

ROOT = pathlib.Path("app/src/main/java/com/example/trainerapp")
ok = []

def patch(path, pairs):
    p = ROOT / path
    text = p.read_text(encoding="utf-8")
    for old, new in pairs:
        if new in text:
            continue
        n = text.count(old)
        if n != 1:
            print(f"[ОШИБКА] {path}: якорь найден {n} раз (нужен ровно 1):\n---\n{old}\n---")
            sys.exit(1)
        text = text.replace(old, new)
    p.write_text(text, encoding="utf-8")
    ok.append(path)

patch("data/model/Models.kt", [(
    '    val metricType: String = "REPS" // REPS, WEIGHT, TIME, DISTANCE, CALORIES\n)',
    '    val metricType: String = "REPS", // REPS, WEIGHT, TIME, DISTANCE, CALORIES\n'
    '    val supersetGroup: Int? = null   // null = одиночное; одинаковый номер = связка A1/A2/A3\n)'
)])

patch("data/local/AppDatabase.kt", [
    ("    version = 13,", "    version = 14,"),
    (
        "        fun getDatabase(context: Context): AppDatabase {",
        "        private val MIGRATION_13_14 = object : Migration(13, 14) {\n"
        "            override fun migrate(db: SupportSQLiteDatabase) {\n"
        "                db.execSQL(\"ALTER TABLE `template_exercises` ADD COLUMN `supersetGroup` INTEGER\")\n"
        "            }\n"
        "        }\n\n"
        "        fun getDatabase(context: Context): AppDatabase {"
    ),
    (
        ".addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)",
        ".addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)"
    ),
])

patch("ui/screens/WorkoutDetailViewModel.kt", [
    (
        "    val exType: StateFlow<String> = _exType.asStateFlow()",
        "    val exType: StateFlow<String> = _exType.asStateFlow()\n\n"
        "    private val _exInSuperset = MutableStateFlow(false)\n"
        "    val exInSuperset: StateFlow<Boolean> = _exInSuperset.asStateFlow()"
    ),
    (
        '            _exType.value = if (ex.targetSets == 0) "CIRCUIT" else "LINEAR"',
        '            _exType.value = if (ex.targetSets == 0) "CIRCUIT" else "LINEAR"\n'
        "            _exInSuperset.value = ex.supersetGroup != null"
    ),
    (
        '            _exType.value = if (block.blockType == "CIRCUIT") "CIRCUIT" else "LINEAR"',
        '            _exType.value = if (block.blockType == "CIRCUIT") "CIRCUIT" else "LINEAR"\n'
        "            _exInSuperset.value = false"
    ),
    (
        "    fun onTypeChange(type: String) { _exType.value = type }",
        "    fun onTypeChange(type: String) { _exType.value = type }\n"
        "    fun onSupersetChange(inSuperset: Boolean) { _exInSuperset.value = inSuperset }"
    ),
    (
        "        if (!isCircuit && (reps.isEmpty() || sets <= 0)) return",
        "        if (!isCircuit && (reps.isEmpty() || sets <= 0)) return\n\n"
        "        val inSuperset = _exInSuperset.value && !isCircuit\n"
        "        var supersetGroup: Int? = null\n"
        "        if (inSuperset) {\n"
        "            val all = dao.getExercisesForBlockSync(blockId).sortedBy { it.order }\n"
        "            val others = if (existing != null) all.filter { it.id != existing.id } else all\n"
        "            val prev = others.lastOrNull()\n"
        "            supersetGroup = prev?.supersetGroup\n"
        "                ?: ((others.mapNotNull { it.supersetGroup }.maxOrNull() ?: 0) + 1)\n"
        "        }"
    ),
    (
        "                            targetReps = reps,\n"
        "                            note = note.ifEmpty { null },\n"
        "                            metricType = metric\n"
        "                        )",
        "                            targetReps = reps,\n"
        "                            note = note.ifEmpty { null },\n"
        "                            metricType = metric,\n"
        "                            supersetGroup = supersetGroup\n"
        "                        )"
    ),
    (
        "                            targetWeight = null,\n"
        "                            note = note.ifEmpty { null },\n"
        "                            metricType = metric\n"
        "                        )",
        "                            targetWeight = null,\n"
        "                            note = note.ifEmpty { null },\n"
        "                            metricType = metric,\n"
        "                            supersetGroup = supersetGroup\n"
        "                        )"
    ),
])

patch("ui/screens/WorkoutDetailScreen.kt", [
    (
        "    val blockError by viewModel.blockError.collectAsState()",
        "    val blockError by viewModel.blockError.collectAsState()\n"
        "    val exInSuperset by viewModel.exInSuperset.collectAsState()"
    ),
    (
        "                    Row(\n"
        "                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),\n"
        "                        horizontalArrangement = Arrangement.spacedBy(6.dp)\n"
        "                    ) {\n"
        "                        metricLabels.forEach { (value, label) ->\n"
        "                            FilterChip(\n"
        "                                selected = exMetric == value,\n"
        "                                onClick = { viewModel.onMetricChange(value) },\n"
        "                                label = { Text(label) }\n"
        "                            )\n"
        "                        }\n"
        "                    }",
        "                    Row(\n"
        "                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),\n"
        "                        horizontalArrangement = Arrangement.spacedBy(6.dp)\n"
        "                    ) {\n"
        "                        metricLabels.forEach { (value, label) ->\n"
        "                            FilterChip(\n"
        "                                selected = exMetric == value,\n"
        "                                onClick = { viewModel.onMetricChange(value) },\n"
        "                                label = { Text(label) }\n"
        "                            )\n"
        "                        }\n"
        "                    }\n"
        "                    if (!blockIsCircuit) {\n"
        "                        Spacer(modifier = Modifier.height(8.dp))\n"
        "                        FilterChip(\n"
        "                            selected = exInSuperset,\n"
        "                            onClick = { viewModel.onSupersetChange(!exInSuperset) },\n"
        '                            label = { Text("В связку с предыдущим") }\n'
        "                        )\n"
        "                    }"
    ),
    (
        "                                } else {\n"
        "                                    exercises.forEach { ex ->",
        "                                } else {\n"
        "                                    val groupOrder = exercises.mapNotNull { it.supersetGroup }.distinct()\n"
        "                                    val exTags = exercises.associate { ex ->\n"
        "                                        ex.id to ex.supersetGroup?.let { g ->\n"
        "                                            ('A' + groupOrder.indexOf(g)).toString() +\n"
        "                                                (exercises.filter { it.supersetGroup == g }.indexOf(ex) + 1)\n"
        "                                        }\n"
        "                                    }\n"
        "                                    exercises.forEach { ex ->"
    ),
    (
        "                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {\n"
        "                                                Column(modifier = Modifier.weight(1f)) {",
        "                                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {\n"
        "                                                exTags[ex.id]?.let { tag ->\n"
        "                                                    Text(tag, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)\n"
        "                                                    Spacer(modifier = Modifier.width(8.dp))\n"
        "                                                }\n"
        "                                                Column(modifier = Modifier.weight(1f)) {"
    ),
])

theme = ROOT / "ui/theme/Theme.kt"
t = theme.read_text(encoding="utf-8")
if "LocalContentColor" not in t:
    t = t.replace(
        "import androidx.compose.material3.MaterialTheme\nimport androidx.compose.runtime.Composable",
        "import androidx.compose.material3.LocalContentColor\n"
        "import androidx.compose.material3.MaterialTheme\n"
        "import androidx.compose.runtime.CompositionLocalProvider\n"
        "import androidx.compose.runtime.Composable",
    )
    t = t.replace(
        "        content = content",
        "        content = { CompositionLocalProvider(LocalContentColor provides colorSchemeFor(themeMode).onSurface) { content() } }",
    )
    theme.write_text(t, encoding="utf-8")
    ok.append("ui/theme/Theme.kt")
else:
    print("[skip] ui/theme/Theme.kt: LocalContentColor уже есть")

print("\nГотово. Изменены файлы:")
for f in ok:
    print("  -", f)
