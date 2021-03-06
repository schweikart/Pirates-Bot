package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import me.zoemartin.piratesBot.modules.commandProcessing.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfo implements GuildCommand {
    @Override
    public String name() {
        return "userinfo";
    }

    @Override
    public String regex() {
        return "i|userinfo|info|profile";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.isEmpty() || args.size() == 1,
            CommandArgumentException::new);
        User u;
        if (args.isEmpty()) u = user;
        else u = Parser.User.isParsable(args.get(0)) ? CacheUtils.getUser(args.get(0))
                     : Bot.getJDA().getUserByTag(args.get(0));
        if (u == null) u = user;
        Member member = CacheUtils.getMember(original.getGuild(), u.getId());

        EmbedBuilder eb;
        if (member == null) {
            eb = new EmbedBuilder()
                     .setAuthor(u.getAsTag(), null, u.getEffectiveAvatarUrl())
                     .setDescription(u.getAsMention())
                     .setThumbnail(u.getAvatarUrl())
                     .setFooter("ID: " + u.getId())
                     .setTimestamp(Instant.now())
                     .addField("Username", u.getAsTag(), true)
                     .addField("Avatar",
                         String.format("[Link](%s)", u.getEffectiveAvatarUrl()), true)
                     .addField("Registered at",
                         Timestamp.valueOf(u.getTimeCreated().atZoneSameInstant(ZoneOffset.UTC)
                                               .toLocalDateTime()) + " UTC",
                         true)
                     .addField("Account Age", MessageUtils.dateAgo(u.getTimeCreated(),
                         OffsetDateTime.now()), true);

        } else {
            eb = new EmbedBuilder()
                     .setAuthor(u.getAsTag(), null, u.getEffectiveAvatarUrl())
                     .setColor(member.getColor())
                     .setDescription(u.getAsMention())
                     .setThumbnail(u.getAvatarUrl())
                     .setFooter("ID: " + u.getId())
                     .setTimestamp(Instant.now())
                     .addField("Username", u.getAsTag(), true)
                     .addField("Nickname", member.getEffectiveName(), true)
                     .addField("Avatar", String.format("[Link](%s)", u.getEffectiveAvatarUrl()), true)
                     .addField("Highest Role",
                         member.getRoles().isEmpty() ? "n/a" : member.getRoles().get(0).getAsMention(), true);

            MemberPermission mp = PermissionHandler.getMemberPerm(original.getGuild().getId(), member.getId());
            CommandPerm mcp = mp.getPerm();

            CommandPerm rp = CommandPerm.fromNum(
                PermissionHandler.getRolePerms(
                    original.getGuild().getId()).stream().filter(
                    rolePermission -> member.getRoles().stream()
                                          .anyMatch(role -> role.getId().equals(rolePermission.getRole_id()))
                ).map(RolePermission::getPerm)
                    .map(CommandPerm::raw).max(Integer::compareTo).orElse(0));

            if (mcp.raw() >= rp.raw() && mcp.raw() > 0)
                eb.addField("Bot Perm", String.format("`[%d] %s`", mp.getPerm().raw(), mp.getPerm().toString()), true);
            else if (rp.raw() >= mcp.raw() && rp.raw() > 0)
                eb.addField("Bot Perm", String.format("`[%d] %s`", rp.raw(), rp.toString()), true);


            eb.addField("Registered at",
                Timestamp.valueOf(u.getTimeCreated().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()) + " UTC",
                true)
                .addField("Account Age", MessageUtils.dateAgo(u.getTimeCreated(), OffsetDateTime.now()), true)
                .addField("Joined at",
                    Timestamp.valueOf(member.getTimeJoined().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime())
                        + " UTC", true)
                .addField("Joined Server Age", MessageUtils.dateAgo(member.getTimeJoined(), OffsetDateTime.now())
                                                   + "\n"
                                                   + ChronoUnit.DAYS.between(member.getTimeJoined(), OffsetDateTime.now())
                                                   + " days after the server was created", true);

            String roles = member.getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", "));
            eb.addField(String.format("Roles (%s)", member.getRoles().size()),
                roles.length() <= 1024 ? roles : "Too many to list", false);

        }
        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "userinfo [user]";
    }

    @Override
    public String description() {
        return "Gives Information about a user";
    }
}
