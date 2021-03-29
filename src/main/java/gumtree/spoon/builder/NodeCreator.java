package gumtree.spoon.builder;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.github.gumtreediff.tree.Tree;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

/**
 * responsible to add additional nodes only overrides scan* to add new nodes
 */
public class NodeCreator extends CtInheritanceScanner {
	public static final String MODIFIERS = "Modifiers_";
	private final TreeScanner builder;

	NodeCreator(TreeScanner builder) {
		this.builder = builder;
	}

	@Override
	public void scanCtModifiable(CtModifiable m) {

		if (m.getModifiers().isEmpty()) {
			return;

		}
		// We add the type of modifiable element
		String type = MODIFIERS + getClassName(m.getClass().getSimpleName()) + "_" + m.getShortRepresentation();

		// String type = MODIFIERS + getClassName(m.getClass().getSimpleName()) + "_" +
		// m.getShortRepresentation();
		Tree modifiers = builder.createNode(type, "");

		// We create a virtual node
		modifiers.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, new CtVirtualElement(type, m, m.getModifiers()));

		// ensuring an order (instead of hashset)
		// otherwise some flaky tests in CI
		Set<ModifierKind> modifiers1 = new TreeSet<>(new Comparator<ModifierKind>() {
			@Override
			public int compare(ModifierKind o1, ModifierKind o2) {
				return o1.name().compareTo(o2.name());
			}
		});
		modifiers1.addAll(m.getModifiers());

		for (ModifierKind kind : modifiers1) {
			// TODO: I added the kind of the modifier in the type
			Tree modifier = builder.createNode("Modifier_" + kind.toString(), kind.toString());
			modifiers.addChild(modifier);
			// We wrap the modifier (which is not a ctelement)
			modifier.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, new CtWrapper(kind, m));
		}
		builder.addSiblingNode(modifiers);

	}

	private String getNodeType(CtElement element) {
		String nodeTypeName = "";
		if (element != null) {
			nodeTypeName = getTypeName(element.getClass().getSimpleName());
		}
		if (element instanceof CtBlock) {
			nodeTypeName = element.getRoleInParent().toString();
		}
		return nodeTypeName;
	}

	private String getClassName(String simpleName) {
		if (simpleName == null)
			return "";
		return simpleName.replace("Ct", "").replace("Impl", "");
	}

	@Override
	public <T> void scanCtVariable(CtVariable<T> e) {

		CtTypeReference<T> type = e.getType();
		if (type != null) {
			// TODO: workaround to avoid NPE
			Tree variableType = builder.createNode(getNodeType(type), type.getQualifiedName());

			// Tree variableType = builder.createNode("VARIABLE_TYPE",
			// type.getQualifiedName());
			variableType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, variableType);
			builder.addSiblingNode(variableType);
		}
	}

	private String getTypeName(String simpleName) {
		// Removes the "Ct" at the beginning and the "Impl" at the end.
		return simpleName.substring(2, simpleName.length() - 4);
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> e) {

		// add the return type of the method
		CtTypeReference<T> type = e.getType();
		if (type != null) {
			Tree returnType = builder.createNode("RETURN_TYPE", type.getQualifiedName());
			returnType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, type);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, returnType);
			builder.addSiblingNode(returnType);
		}

		for (CtTypeReference thrown : e.getThrownTypes()) {
			Tree thrownType = builder.createNode("THROWS", thrown.getQualifiedName());
			thrownType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, thrown);
			type.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, thrownType);
			builder.addSiblingNode(thrownType);
		}

		super.visitCtMethod(e);
	}

	@Override
	public void scanCtReference(CtReference reference) {

		if (reference instanceof CtTypeReference && reference.getRoleInParent() == CtRole.SUPER_TYPE) {
			Tree superType = builder.createNode("SUPER_TYPE", reference.toString());
			CtWrapper<CtReference> k = new CtWrapper<CtReference>(reference, reference.getParent());
			superType.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, k);
			reference.putMetadata(SpoonGumTreeBuilder.GUMTREE_NODE, superType);
			builder.addSiblingNode(superType);
		} else {
			super.scanCtReference(reference);
		}
	}

}
