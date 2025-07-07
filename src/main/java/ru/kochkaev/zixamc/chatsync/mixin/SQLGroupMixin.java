package ru.kochkaev.zixamc.chatsync.mixin;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineExceptionHandler;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.sync.Mutex;
import kotlinx.coroutines.sync.MutexImpl;
import kotlinx.coroutines.sync.MutexKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.kochkaev.zixamc.api.Initializer;
import ru.kochkaev.zixamc.api.sql.SQLGroup;
import ru.kochkaev.zixamc.chatsync.LastMessage;
import ru.kochkaev.zixamc.requests.ChatSyncSQLGroup;

import java.util.concurrent.CompletionException;

@Mixin(SQLGroup.class)
public class SQLGroupMixin implements ChatSyncSQLGroup {

    @Unique @Nullable
    private LastMessage lastMessage = null;
    @Unique @NotNull
    private final Mutex lastMessageLock = new MutexImpl(false);

    @Override
    public @Nullable LastMessage chatsync$getLastMessage() {
        return lastMessage;
    }

    @Override
    public void chatsync$setLastMessage(@Nullable LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public @NotNull Mutex chatsync$getLastMessageLock() {
        return lastMessageLock;
    }
}
