package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableConsumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.pdg.adventure.model.CommandData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * The overview grid of commands shared by {@link CommandsMenuView} (location/item commands) and
 * {@code com.pdg.adventure.view.workflow.CommandListEditorView} (workflow, arrival and response
 * commands): Verb / Adjective / Noun / Preconditions / Actions columns, rows presented
 * alphabetically, and the double-click-to-edit plus Edit/Delete context menu behaviour. What an
 * edit or a delete actually does differs per screen, so the owning view hands those in via
 * {@link #setEditAndDeleteHandlers}.
 */
public class CommandGrid extends Grid<CommandData> {

    // Presentation order: alphabetical by verb, then adjective, then noun - matches the
    // Verb/Adjective/Noun column order. Purely cosmetic: it has no effect on which command fires
    // (CommandExecutor fully disambiguates or asks the player to clarify; Workflow.process()
    // orders its preCommands the same way independently).
    static final Comparator<CommandData> ALPHABETICAL = Comparator
            .comparing((CommandData cmd) -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()),
                       String.CASE_INSENSITIVE_ORDER)
            .thenComparing(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()),
                            String.CASE_INSENSITIVE_ORDER)
            .thenComparing(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()),
                            String.CASE_INSENSITIVE_ORDER);

    // Set by the owning view once the adventure is resolved (in beforeEnter), after this grid has
    // been constructed - the renderers below read it lazily. Transient like in the views: it is
    // rebuilt on the next beforeEnter rather than serialized with the session.
    private transient PreconditionActionFormatter formatter;

    public CommandGrid(String anEmptyStateText) {
        super(CommandData.class, false);
        addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getVerb()))
                .setHeader("Verb").setAutoWidth(true);
        addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getAdjective()))
                .setHeader("Adjective").setAutoWidth(true);
        addColumn(cmd -> ViewSupporter.getWordText(cmd.getCommandDescription().getNoun()))
                .setHeader("Noun").setAutoWidth(true);
        addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatConditions(cmd.getPreConditions()))))
                .setHeader("Preconditions").setAutoWidth(true);
        addColumn(new ComponentRenderer<>(cmd -> stack(formatter.formatActions(cmd.getActions()))))
                .setHeader("Actions").setAutoWidth(true);
        addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        setEmptyStateText(anEmptyStateText);
        ViewSupporter.setSize(this);
    }

    public void setFormatter(PreconditionActionFormatter aFormatter) {
        formatter = aFormatter;
    }

    /**
     * Shows the given commands sorted alphabetically. The returned data view is backed by the
     * sorted copy, so callers that remove a row can do so through it without rebuilding the grid.
     */
    public GridListDataView<CommandData> showCommands(Collection<CommandData> aCommands) {
        List<CommandData> sorted = new ArrayList<>(aCommands);
        sorted.sort(ALPHABETICAL);
        return setItems(sorted);
    }

    /** Double-click and the context menu's "Edit" run {@code anEditHandler}; "Delete" runs {@code aDeleteHandler}. */
    public void setEditAndDeleteHandlers(SerializableConsumer<CommandData> anEditHandler,
                                         SerializableConsumer<CommandData> aDeleteHandler) {
        addItemDoubleClickListener(e -> anEditHandler.accept(e.getItem()));

        GridContextMenu<CommandData> menu = new GridContextMenu<>(this);
        menu.addItem("Edit", e -> e.getItem().ifPresent(anEditHandler));
        menu.addComponent(new Hr());
        menu.addItem("Delete", e -> e.getItem().ifPresent(aDeleteHandler));
    }

    /** Stack each rendered line in its own Span so multi-entry precondition/action cells wrap vertically. */
    static Component stack(List<String> lines) {
        Div box = new Div();
        box.getStyle().set("display", "flex").set("flex-direction", "column");
        lines.forEach(line -> box.add(new Span(line)));
        return box;
    }
}
