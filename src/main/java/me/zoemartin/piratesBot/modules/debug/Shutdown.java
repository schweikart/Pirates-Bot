package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.util.Check;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Shutdown implements Command {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "shutdown";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.isEmpty() || args.size() == 1 && args.get(0).matches("force|now"),
            CommandArgumentException::new);

        if (args.isEmpty()) {
            channel.sendMessageFormat("Shutting down soon! :)").queue();
            Bot.getJDA().shutdown();
        } else {
            channel.sendMessageFormat("Shutting down now!").complete();
            Bot.getJDA().shutdownNow();
        }
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.OWNER;
    }

    @Override
    public String usage() {
        return "shutdown [force|now]";
    }

    @Override
    public String description() {
        return "Shuts down the bot";
    }
}
