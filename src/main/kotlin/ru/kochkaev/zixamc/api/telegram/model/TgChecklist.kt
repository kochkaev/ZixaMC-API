package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes a checklist. */
data class TgChecklist(
    /** Title of the checklist */
    val title: String,
    /** Special entities that appear in the checklist title */
    @SerializedName("title_entities")
    val titleEntities: List<TgEntity>?,
    /** List of tasks in the checklist */
    @SerializedName("tasks")
    val tasks: List<TgChecklistTask>,
    /** True, if users other than the creator of the list can add tasks to the list */
    @SerializedName("others_can_add_tasks")
    val othersCanAddTasks: Boolean?,
    /** True, if users other than the creator of the list can mark tasks as done or not done */
    @SerializedName("others_can_mark_tasks_as_done")
    val othersCanMarkTasksAsDone: Boolean?,
)
/** Describes a task in a checklist. */
data class TgChecklistTask(
    /** Unique identifier of the task */
    val id: Int,
    /** Text of the task */
    val text: String,
    /** Special entities that appear in the task text */
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>?,
    /** User that completed the task; omitted if the task wasn't completed */
    @SerializedName("completed_by_user")
    val completedByUser: TgUser?,
    /** Point in time (Unix timestamp) when the task was completed; 0 if the task wasn't completed */
    @SerializedName("completion_date")
    val completionDate: Int?,
)
/** Describes a service message about checklist tasks marked as done or not done. */
data class TgChecklistTasksDone(
    /** Message containing the checklist whose tasks were marked as done or not done. Note that the Message object in this field will not contain the reply_to_message field even if it itself is a reply. */
    @SerializedName("checklist_message")
    val checklistMessage: TgMessage?,
    /** Identifiers of the tasks that were marked as done */
    @SerializedName("marked_as_done_task_ids")
    val markedAsDoneTaskIds: List<Int>?,
    /** Identifiers of the tasks that were marked as not done */
    @SerializedName("marked_as_not_done_task_ids")
    val markedAsNotDoneTaskIds: List<Int>?,
)
/** Describes a service message about tasks added to a checklist. */
data class TgChecklistTasksAdded(
    /** Message containing the checklist to which the tasks were added. Note that the Message object in this field will not contain the reply_to_message field even if it itself is a reply. */
    @SerializedName("checklist_message")
    val checklistMessage: TgMessage?,
    /** List of tasks added to the checklist */
    val tasks: List<TgChecklistTask>,
)

/** Describes a checklist. */
data class TgInputChecklist(
    /** Title of the checklist; 1-255 characters after entities parsing */
    val title: String,
    /** Mode for parsing entities in the text. See formatting options for more details. */
    @SerializedName("parse_mode")
    val parseMode: String? = "HTML",
    /** List of special entities that appear in the title, which can be specified instead of parse_mode. Currently, only bold, italic, underline, strikethrough, spoiler, and custom_emoji entities are allowed. */
    @SerializedName("title_entities")
    val titleEntities: List<TgEntity>? = null,
    /** List of 1-30 tasks in the checklist */
    @SerializedName("tasks")
    val tasks: List<TgInputChecklistTask>,
    /** Pass True if other users can add tasks to the checklist */
    @SerializedName("others_can_add_tasks")
    val othersCanAddTasks: Boolean? = null,
    /** Pass True if other users can mark tasks as done or not done in the checklist */
    @SerializedName("others_can_mark_tasks_as_done")
    val othersCanMarkTasksAsDone: Boolean? = null,
)
/** Describes a task in a checklist. */
data class TgInputChecklistTask(
    /** Unique identifier of the task; must be positive and unique among all task identifiers currently present in the checklist */
    val id: Int,
    /** Text of the task; 1-100 characters after entities parsing */
    val text: String,
    /** Mode for parsing entities in the text. See formatting options for more details. */
    @SerializedName("parse_mode")
    val parseMode: String? = "HTML",
    /** List of special entities that appear in the text, which can be specified instead of parse_mode. Currently, only bold, italic, underline, strikethrough, spoiler, and custom_emoji entities are allowed. */
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>? = null,
)
