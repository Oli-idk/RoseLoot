package dev.rosewood.roseloot.command.command;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseloot.loot.LootTable;
import dev.rosewood.roseloot.loot.table.LootTableType;
import dev.rosewood.roseloot.manager.LocaleManager;
import dev.rosewood.roseloot.manager.LootTableManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.command.CommandSender;

public class ListCommand extends RoseCommand {

    public ListCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        LocaleManager localeManager = this.rosePlugin.getManager(LocaleManager.class);
        LootTableManager lootTableManager = this.rosePlugin.getManager(LootTableManager.class);

        List<LootTable> lootTables = lootTableManager.getLootTables();
        if (lootTables.isEmpty()) {
            localeManager.sendMessage(context.getSender(), "command-list-none");
            return;
        }

        TreeBranch treeBranch = new TreeBranch(lootTableManager);
        lootTables.forEach(x -> treeBranch.add(x.getName(), x));
        localeManager.sendMessage(context.getSender(), "command-list-header", StringPlaceholders.single("amount", lootTables.size()));
        treeBranch.traverse(context.getSender(), localeManager, 1);
    }

    @Override
    protected String getDefaultName() {
        return "list";
    }

    @Override
    protected List<String> getDefaultAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getDescriptionKey() {
        return "command-list-description";
    }

    @Override
    public String getRequiredPermission() {
        return "roseloot.list";
    }

    /**
     * Sorts LootTables by directory for display in chat
     */
    private static class TreeBranch {

        private final LootTableManager lootTableManager;
        private final Map<String, TreeBranch> branches; // Name -> Branch
        private final Multimap<TreeBranchType, String> leaves; // Type -> Name

        public TreeBranch(LootTableManager lootTableManager) {
            this.lootTableManager = lootTableManager;
            this.branches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            this.leaves = TreeMultimap.create(TreeBranchType::compareTo, String.CASE_INSENSITIVE_ORDER);
        }

        public void add(String name, LootTable lootTable) {
            int index = name.indexOf('/');
            if (index == -1) {
                LootTableType type = lootTable.getType();
                String typeName = this.lootTableManager.getLootTableTypeName(type);
                this.leaves.put(new TreeBranchType(type, typeName), name);
            } else {
                String branchLocation = name.substring(0, index);
                String branchName = name.substring(index + 1);
                TreeBranch branch = this.branches.get(branchLocation);
                if (branch == null) {
                    branch = new TreeBranch(this.lootTableManager);
                    this.branches.put(branchLocation, branch);
                }
                branch.add(branchName, lootTable);
            }
        }

        public void traverse(CommandSender sender, LocaleManager localeManager, int depth) {
            StringBuilder paddingBuilder = new StringBuilder();
            String spacer = localeManager.getLocaleMessage("command-list-hierarchy-spacer");
            for (int i = 0; i < depth; i++)
                paddingBuilder.append(spacer);
            String padding = paddingBuilder.toString();

            // Print leaves
            for (TreeBranchType type : this.leaves.keySet())
                for (String name : this.leaves.get(type))
                    localeManager.sendSimpleMessage(sender, "command-list-hierarchy-leaf", StringPlaceholders.builder("name", name).addPlaceholder("type", type.getName()).addPlaceholder("spacer", padding).build());

            // Print branches
            for (Map.Entry<String, TreeBranch> entry : this.branches.entrySet()) {
                localeManager.sendSimpleMessage(sender, "command-list-hierarchy-branch", StringPlaceholders.builder("name", entry.getKey()).addPlaceholder("spacer", padding).build());
                entry.getValue().traverse(sender, localeManager, depth + 1);
            }
        }

    }

    private static class TreeBranchType implements Comparable<TreeBranchType> {

        private final LootTableType type;
        private final String name;

        public TreeBranchType(LootTableType type, String name) {
            this.type = type;
            this.name = name;
        }

        public LootTableType getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public int compareTo(ListCommand.TreeBranchType o) {
            return this.name.compareToIgnoreCase(o.name);
        }

    }

}
