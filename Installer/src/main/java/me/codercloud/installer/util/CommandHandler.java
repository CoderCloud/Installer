package me.codercloud.installer.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

/**
 * A bukkit CommandHandler library
 * 
 * When using this library it would be nice if you would mention it on your project page :D Thanks
 * @author CoderCloud
 */
public class CommandHandler {
	
	private final CommandListener[] cmds;
	private final String unknownCommand;
	
	public CommandHandler(Collection<CommandListener> cmds) {
		this(null, cmds);
	}
	
	public CommandHandler(String unknownMessage, Collection<CommandListener> cmds) {
		this(unknownMessage, cmds == null ? new CommandListener[0] : cmds.toArray(new CommandListener[cmds.size()]));
	}
	
	public CommandHandler(CommandListener... cmds) {
		this(null, cmds);
	}
	
	public CommandHandler(String unknownMessage, CommandListener... cmds) {
		this.cmds = cmds;
		this.unknownCommand = unknownMessage;
	}
	
	public boolean handleCommand(CommandSender sender, Command command,
			String label, String[] args) {
		boolean found = false;;
		for (CommandListener cmd : cmds) {
			if (cmd.isCommand(command, args)) {
				if (cmd.hasPermission(sender))
					cmd.handleCommand(this, sender, command, label, args);
				else
					cmd.sendPermissionMessage(sender);
				found = true;
			}
		}
		
		StringBuilder cmd = new StringBuilder().append("/").append(label);
		for(String s : args)
			cmd.append(" ").append(s);
		
		if(!found && unknownCommand != null)
			sender.sendMessage(unknownCommand.replaceAll("<cmd>", "'" + cmd + "'").replaceAll("<user>", sender.getName()));
		return true;
	}
	
	public List<String> handleTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		ArrayList<String> strings = new ArrayList<String>();
		
		for (CommandListener cmd : cmds) {
			cmd.tabComplete(strings, sender, command, label, args);
		}
		
		return strings;
	}
	
	private String head = ChatColor.YELLOW + "Help (Page <page>/<maxpage>):";
	private String seperator = " -> ";
	private String noCommand = ChatColor.RED + "No commands found";
	private ChatColor c1 = ChatColor.GREEN, c2 = ChatColor.DARK_GREEN;
	
	public void setDefaultHelpFormat(String head, String seperator, String noCommandFound, ChatColor flip1, ChatColor flip2) {
		this.head = head;
		this.seperator = seperator;
		this.noCommand = noCommandFound;
		this.c1 = flip1;
		this.c2 = flip2;
	}
	
	public String[] getHelp(Permissible p, int page, int perPage) {
		return getHelp(p, page, perPage, head, seperator, noCommand, c1, c2);
	}
	
	public String[] getHelp(Permissible p, int page, int perPage, String head, String seperator, String noCommandsFound, ChatColor flip1, ChatColor flip2) {
		ArrayList<CommandListener> cmds = new ArrayList<CommandListener>();
		ArrayList<String> msg = new ArrayList<String>();
		for (CommandListener c : this.cmds)
			if (p == null || c.hasPermission(p))
				cmds.add(c);
		if (cmds.size() == 0) {
			if (head == null) {
				if (noCommandsFound == null)
					return new String[0];
				msg.add(noCommandsFound);
			} else {
				if (noCommandsFound == null) {
					msg.add(head.replaceAll("<page>", "0").replaceAll("<maxpage>", "0"));
				} else {
					msg.add(head.replaceAll("<page>", "0").replaceAll("<maxpage>", "0"));
					msg.add(noCommandsFound);
				}
			}
			return msg.toArray(new String[msg.size()]);
		}

		int maxPage = (cmds.size() - 1) / perPage + 1;
		if (page < 1)
			page = 1;
		if (page > maxPage)
			page = maxPage;
		int offs = (page - 1) * perPage;
		int len = cmds.size() - offs;
		if(len > perPage)
			len = perPage;

		if (head != null)
			msg.add(head.replaceAll("<page>", String.valueOf(page)).replaceAll("<maxpage>", String.valueOf(maxPage)));
		for (int i = 0; i < len; i++) {
			CommandListener args = cmds.get(i + offs);
			String name = args.getCommandFormat();
			String desc = args.getDescription();
			StringBuilder b = new StringBuilder();
			b.append(((i % 2 == 1) ? (c1 == null ? ChatColor.RESET : c1) : (c1 == null ? ChatColor.RESET : c2)));
			b.append(name);
			if (desc != null) {
				if (seperator == null)
					b.append(" -> ");
				else
					b.append(seperator);
				b.append(desc);
			}
			msg.add(b.toString());
		}
		return msg.toArray(new String[msg.size()]);
	}
	
	public static abstract class CommandListener {
		
		private final String command;
		private final String[] args;
		private final boolean[] isVar;
		private final HashMap<String, Integer> varNames = new HashMap<String, Integer>();
		
		public CommandListener(String command) {
			if(command.startsWith("/"))
				command = command.replaceFirst("/", "");
			String[] args;
			if(command.indexOf(" ") == -1) {
				args = new String[0];
			}else{
				args = removeEmptyStrings(command.substring(command.indexOf(" ")+1, command.length()).split(" "));
				command = command.substring(0, command.indexOf(" "));
			}
			boolean[] isVar = new boolean[args.length];
			
			for(int i = 0; i<args.length; i++) {
				String arg = args[i];
				if(arg != null && arg.startsWith("<var=") && arg.endsWith(">")) {
					isVar[i] = true;
					args[i] = arg.substring(5, arg.length()-1).toLowerCase();
					if(args[i].equals("<...>"))
						throw new IllegalArgumentException("A var cant be named '<...>'");
					if(varNames.containsKey(args[i]))
						throw new IllegalArgumentException("You cant have two vars named '" + args[i] + "'");
					varNames.put(args[i], i);
				} else if(args[i].equals("<...>")) {
					isVar[i] = true;
				} else {
					isVar[i] = false;
				}
			}
			
			this.command = command;
			this.args = args;
			this.isVar = isVar;
		}
		
		public abstract void handleCommand(CommandHandler h, CommandSender sender, Command command, String label, String[] args);
		
		public void sendPermissionMessage(CommandSender s) {
			s.sendMessage(ChatColor.RED + "You are not permitted :(");
		}
		
		public boolean hasPermission(Permissible p) {
			return true;
		}
		
		public Collection<Object> getArgumentOptions() {
			return null;
		}
		
		public void tabCompleteEnd(ArrayList<String> add, CommandSender s, Command c, String label, String[] args, String current, int endoffs) {
			
		}
		
		public String getDescription() {
			return null;
		}
		
		public final String getCommandFormat() {
			String cmd = "/" + command;
			for(int i = 0; i<args.length; i++) {
				if(isVar[i]) {
					if(args[i].equals("<...>"))
						cmd += " " + "...";
					else
						cmd += " [" + args[i].toUpperCase() + "]";
				} else
					cmd += " " + args[i];
			}
			
			return cmd;
		}
		
		public final boolean isCommand(Command cmd, String[] args) {
			if(!cmd.getName().equalsIgnoreCase(command))
			    return false;
			int i;
			for(i = 0; i<this.args.length; i++) {
				if(isVar[i])
					continue;
				if(!(i<args.length))
					return false;
				String arg = this.args[i];
				if(arg.startsWith("<") && arg.endsWith(">")){
					String[] sp = arg.substring(1, arg.length()-1).split("/");
					boolean b = false;
					for(String s : sp) {
						if(s.equalsIgnoreCase(args[i]))
							b = true;
					}
					if(b)
						continue;
					else
						return false;
				}
				if(!args[i].equalsIgnoreCase(arg))
					return false;
			}
			if(args.length > i)
				if(this.args.length == 0 || !this.args[i-1].equals("<...>"))
					return false;
			return true;
		}
		
		public final boolean isCommandStart(Command cmd, String[] args) {
			if(!cmd.getName().equalsIgnoreCase(command))
			    return false;
			int i;
			for(i = 0; i<args.length; i++) {
				boolean last = i+1 == args.length;

				
				if(!(i<this.args.length))
					if(!this.args[this.args.length-1].equals("<...>"))
						return false;
					else
						return true;
				
				if(isVar[i])
					continue;
				
				String arg = this.args[i];
				if(arg.startsWith("<") && arg.endsWith(">")){
					String[] sp = arg.substring(1, arg.length()-1).split("/");
					boolean b = false;
					for(String s : sp) {
						if(s.equalsIgnoreCase(args[i]) || (last && s.toLowerCase().startsWith(args[i].toLowerCase())))
							b = true;
					}
					if(b)
						continue;
					else
						return false;
				}
				if(last) {
					if (!arg.toLowerCase().startsWith(args[i].toLowerCase()))
						return false;
				} else if(!arg.equalsIgnoreCase(args[i]))
					return false;
			}
			return true;
		}
		
		public final void tabComplete(ArrayList<String> add, CommandSender s, Command c, String label, String[] args) {
			if(isCommandStart(c, args)) {
				if(hasPermission(s)) {
					int i = args.length;
					if(i>this.args.length)
						i = this.args.length;
					
					i--;
					
					String arg = this.args[i];
					
					
					if(isVar[i]) {
						String current = args[args.length-1];
						if(arg.equals("<...>"))
							tabCompleteEnd(add, s, c, label, args, current, args.length-this.args.length);
						else {
							Collection<Object> l = getArgumentOptions();
							String st;
							if(l != null)
								for(Object o : l)
									if((st=o.toString()).toLowerCase().startsWith(current))
										add.add(st);
						}
					} else {
						if(arg.startsWith("<") && arg.endsWith(">")){
							String[] sp = arg.substring(1, arg.length()-1).split("/");
							for(String st : sp) {
								if((st.toLowerCase().startsWith(args[i].toLowerCase())))
									add.add(st);
							}
						} else {
							if((arg.toLowerCase().startsWith(args[i].toLowerCase())))
								add.add(arg);
						}
					}
				}
			}
		}
		
		public final String getVar(String varName, String[] args) {
			if(varName.equals("<...>")) {
				int offs = this.args.length-1;
				if(args.length-offs <= 0)
					return null;
				StringBuilder s = new StringBuilder(args[offs++]);
				for(;offs<args.length; offs++)
					s.append(" ").append(args[offs]);
				return s.toString();
			}
			varName = varName.toLowerCase();
			int loc = varNames.get(varName);
			if(args.length <= loc || loc < 0)
				return null;
			return args[loc];
		}
		
		public final Integer getVarAsInt(String varName, String[] args) {
			String var = getVar(varName, args);
			if(var == null)
				return null;
			int i = 0;
			  int j;
			  int num = 0;
			  boolean isNeg = false;
			  if( var.charAt(i) == '-') {
			    isNeg = true;
			    i++;
			  }
			  
			  while( i < var.length()) {
			    num *= 10;
			    num += j=var.charAt(i++) - '0';
			    if(j>9)
			    	return null;
			  }

			  if (isNeg)
			    num = -num;
			  return num;
		}
		
		@SuppressWarnings("deprecation")
		public final Player getVarAsPlayer(String varName, String[] args) {
			String var = getVar(varName, args);
			return var == null ? null : Bukkit.getPlayer(var);
		}
		
		@SuppressWarnings("deprecation")
		public final Player getVarAsPlayerExact(String varName, String[] args) {
			String var = getVar(varName, args);
			return var == null ? null : Bukkit.getPlayerExact(var);
		}
		
		public final boolean isVarSet(String varName, String[] args) {
			return getVar(varName, args) != null;
		}
		
		public final <C extends Collection<? super String>, A extends Player> C addAllPlayerNames(C c, A[] add) {
			for(Player o : add)
				c.add(o==null?"null":o.getName());
			return c;
		}
		
		public final <C extends Collection<? super String>, A> C addAllToString(C c, A[] add) {
			for(Object o : add)
				c.add(o==null?"null":o.toString());
			return c;
		}
		
		public final <C, A extends C> Collection<C> addAll(Collection<C> c, A[] add) {
			for(C o : add)
				c.add(o);
			return c;
		}
		
		public final Collection<Object> newCollection() {
			return new ArrayDeque<Object>();
		}
		
		public final <T> Collection<T> newCollection(Class<T> t) {
			return new ArrayDeque<T>();
		}
		
		private static String[] removeEmptyStrings(String[] s) {
			ArrayList<String> l = new ArrayList<String>();
			for(String str : s)
				if(str != null && str != "")
					l.add(str);
			return l.toArray(new String[l.size()]);
		}
	}
	
}
