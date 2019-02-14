package us.lsi.tiposrecursivos;

import java.util.Map;
import java.util.function.Function;

import us.lsi.common.Maps2;
import us.lsi.common.Preconditions;


public class BinaryPattern<E> {
	
	
	public enum PatternType{Empty,Leaf,Binary,Variable}
	
	public static <E> BinaryPattern<E> binary(Label<E> label, BinaryPattern<E> left, BinaryPattern<E> right) {
		Preconditions.checkNotNull(label);
		return new BinaryPattern<E>(PatternType.Binary,label, left, right,null);
	}
	
	public static <E> BinaryPattern<E> leaf(Label<E> label) {
		Preconditions.checkNotNull(label);
		return new BinaryPattern<E>(PatternType.Leaf,label, null, null, null);
	}
	
	public static <E> BinaryPattern<E> empty() {
		return new BinaryPattern<E>(PatternType.Empty,null,null, null, null);
	}
	
	public static <E> BinaryPattern<E> variable(String name) {
		Preconditions.checkNotNull(name);
		return new BinaryPattern<E>(PatternType.Variable, null, null, null, VariableExp.variable(name));
	}
	
	public static <E> BinaryPattern<E> parse(String s, Function<String,E> f) {
		Preconditions.checkNotNull(s);
		BinaryTree<String> tree = BinaryTree.parse(s);
		BinaryPattern<E> r = BinaryPattern.toPattern(tree, f);
		return r;
	}
	
	public static <E> BinaryPattern<E> parse(String s) {
		Preconditions.checkNotNull(s);
		BinaryTree<String> tree = BinaryTree.parse(s);
		BinaryPattern<E> r = BinaryPattern.toPattern(tree);
		return r;
	}
	
	private Label<E> label;
	private VariableExp<BinaryTree<E>> varTree;
	private final BinaryPattern<E> left;
	private final BinaryPattern<E> right;
	private PatternType type;
	private Map<String,E> varLabels = null;
	private Map<String,BinaryTree<E>> varTrees = null;
	
	
	private BinaryPattern(PatternType type, Label<E> label, BinaryPattern<E> left, BinaryPattern<E> right, VariableExp<BinaryTree<E>> varTree) {
		super();
		this.type = type;
		this.label = label;
		this.left = left;
		this.right = right;	
		this.varTree = varTree;
	}

	public boolean isEmpty() {
		return type.equals(PatternType.Empty);
	}

	public boolean isLeaf() {
		return type.equals(PatternType.Leaf);
	}

	public boolean isBinary() {
		return type.equals(PatternType.Binary);
	}	
	
	public boolean isVariable() {
		return type.equals(PatternType.Variable);
	}
	
	public Label<E> getLabel() {
		Preconditions.checkArgument(this.isBinary() || this.isLeaf(), "No permitido");
		return label;
	}

	public BinaryPattern<E> getLeft() {
		Preconditions.checkArgument(this.isBinary(), "No permitido");
		return left;
	}

	public BinaryPattern<E> getRight() {
		Preconditions.checkArgument(this.isBinary(), "No permitido");
		return right;
	}	
	
	public VariableExp<BinaryTree<E>> asVariable() {
		Preconditions.checkArgument(this.isVariable(), "No permitido");
		return varTree;
	}

	public PatternType getType() {
		return type;
	}
	
	public Map<String,E> varLabels() {
		if(varLabels == null)
			varLabels = varLabels(this);
		return varLabels;
	}
	
	public Map<String,BinaryTree<E>> varTrees() {
		if(varTrees == null)
			varTrees = varTrees(this);
		return varTrees;
	}
	
	private static <E> Map<String,E> varLabels(BinaryPattern<E> pattern){
		Map<String,E> r = Maps2.newHashMap();
		switch(pattern.getType()) {
		case Leaf: 
			Label<E> label = pattern.getLabel();
			if(label.isVariable()) 
				r = Maps2.newHashMap(label.asVariable().getName(),label.asVariable().getValue()); 
			break;
		case Binary: 
			label = pattern.getLabel();
			r = pattern.getLeft().varLabels();	
			r.putAll(pattern.getRight().varLabels());				
			if(label.isVariable()) {
				r.put(label.asVariable().getName(),label.asVariable().getValue());		
			}
			break;
		default:
		}
		return r;
	}
	
	private static <E> Map<String,BinaryTree<E>> varTrees(BinaryPattern<E> pattern){
		Map<String,BinaryTree<E>> r = Maps2.newHashMap();
		switch(pattern.getType()) {
		case Variable: 
			VariableExp<BinaryTree<E>> varTree = pattern.asVariable();
			r = Maps2.newHashMap(varTree.getName(),varTree.getValue()); 
			break;
		case Binary: 
			r = Maps2.newHashMap(pattern.getLeft().varTrees());
			r.putAll(pattern.getRight().varTrees());
			break;
		default:
		}
		return r;
	}
	
	public String toString(){
		String r = null;;
		switch(this.getType()) {
		case Empty: r ="_"; break;
		case Variable: r = this.asVariable().getName(); break;	
		case Leaf: r = getLabel().toString(); break;
		case Binary: r = getLabel().toString() + "(" + this.getLeft().toString() + "," + this.getRight().toString() + ")"; break;
		}
		return r;
	}

	public static <E> BinaryPattern<E> match(BinaryTree<E> tree, BinaryPattern<E> pt) {
		BinaryPattern<E> r = null; 
		switch(pt.getType()) {
		case Empty: 
			if(tree.isEmpty()) r = pt; break;
		case Variable: 
			pt.asVariable().setValue(tree); r = pt; break;	
		case Leaf: 
			Label<E> label = pt.getLabel();	
			switch(label.type){
			case Constant: if(tree.isLeaf() && tree.getLabel().equals(label.asConstant().getValue())) r = pt;; break;
			case Variable: label.asVariable().setValue(tree.getLabel()); r = pt; break;
			}
			break;
		case Binary: 
			label = pt.getLabel();	
			switch(label.type){
			case Constant: 
				if(tree.isBinary() &&  
					label.asConstant().getValue().equals(tree.getLabel()) && 
					match(tree.getLeft(),pt.getLeft()) !=null && 						 
					match(tree.getRight(),pt.getRight()) !=null) {
					r = pt;
				}
			break;
			case Variable:
				if(tree.isBinary() &&  
					match(tree.getLeft(),pt.getLeft()) != null && 						 
					match(tree.getRight(),pt.getRight()) != null) { 
						label.asVariable().setValue(tree.getLabel());
				    r = pt;
				}
			break;	
			}				
		}
		return r;	
	}
	
	private void reset() {
		this.varTrees = null;this.varLabels = null;
		switch(this.getType()) {
		case Binary: this.getLeft().reset();this.getRight().reset(); break;
		case Empty: break;
		case Leaf: break;
		case Variable: break;
		}
	}
	
	public static <R> BinaryPattern<R> toPattern(BinaryTree<String> tree, Function<String,R> f){
		BinaryPattern<R> r = null;
		switch(tree.getType()) {
		case Empty: r = BinaryPattern.empty(); break;	
		case Leaf: 
			String label = tree.getLabel();
			if (label.charAt(0) != '_') {
				r = BinaryPattern.leaf(Label.constLabel(f.apply(label)));
			} else if  (Character.isLowerCase(label.charAt(1))){
				r = BinaryPattern.leaf(Label.varLabel(label)); 
			} else {
				r = BinaryPattern.variable(label); 
			}
			break;
		case Binary:
			label = tree.getLabel();
			if (label.charAt(0) != '_') {
				r = BinaryPattern.binary(Label.constLabel(f.apply(label)), toPattern(tree.getLeft(),f), toPattern(tree.getRight(),f));
			} else if(Character.isLowerCase(label.charAt(1))) {
				r = BinaryPattern.binary(Label.varLabel(label), toPattern(tree.getLeft(),f), toPattern(tree.getRight(),f));
			} else {
				Preconditions.checkState(false, "No puede haber una etiqueta de �rbol en este lugar");
			}
			break;
		}
		return r;
	}
	
	public static <R> BinaryPattern<R> toPattern(BinaryTree<String> tree){
		BinaryPattern<R> r = null;
		switch(tree.getType()) {
		case Empty: r = BinaryPattern.empty(); break;	
		case Leaf: 
			String label = tree.getLabel();
			if (label.charAt(0) != '_') {
				Preconditions.checkArgument(false, "No es un patr�n sin constantes");
			} else if  (Character.isLowerCase(label.charAt(1))){
				r = BinaryPattern.leaf(Label.varLabel(label)); 
			} else {
				r = BinaryPattern.variable(label); 
			}
			break;
		case Binary:
			label = tree.getLabel();
			if (label.charAt(0) != '_') {
				Preconditions.checkArgument(false, "No es un patr�n sin constantes");
			} else if(Character.isLowerCase(label.charAt(1))) {
				r = BinaryPattern.binary(Label.varLabel(label), toPattern(tree.getLeft()), toPattern(tree.getRight()));
			} else {
				Preconditions.checkState(false, "No puede haber una etiqueta de �rbol en este lugar");
			}
			break;
		}
		return r;
	}
	
	public <R> BinaryPattern<R> map(Function<E,R> f){
		BinaryPattern<R> r = null;
		switch(this.getType()) {
		case Empty: r = BinaryPattern.empty();; break;
		case Variable: r = BinaryPattern.variable(this.asVariable().getName()); break;		
		case Leaf: 
			Label<E> label = this.getLabel();
			switch(label.type){
			case Constant: 
				r = BinaryPattern.leaf(Label.constLabel(f.apply(label.asConstant().getValue()))); break;
			case Variable: 
				r = BinaryPattern.leaf(Label.varLabel(label.asVariable().getName())); break;
			}
		break;
		case Binary: 
			label = this.getLabel();
			switch(label.type){
			case Constant: r = BinaryPattern.binary(Label.constLabel(f.apply(label.asConstant().getValue())), 
					this.getLeft().map(f), 
					this.getRight().map(f)); 
			break;
			case Variable: 
				r = BinaryPattern.binary(Label.varLabel(label.asVariable().getName()), 
					this.getLeft().map(f), 					
					this.getRight().map(f)); 
			break;
			}
		}
		return r;
	}
	
	public BinaryTree<E> valuesToVariables(){
		BinaryTree<E> r = null;
		switch(this.getType()) {
		case Empty: r = BinaryTree.empty();; break;
		case Variable: r = this.asVariable().getValue(); break;		
		case Leaf:
			Label<E> label = this.getLabel();
			switch(label.getType()) {
			case Constant: r = BinaryTree.leaf(label.asConstant().getValue()); break;
			case Variable: r = BinaryTree.leaf(label.asVariable().getValue()); break;
			}
		case Binary: 
			label = this.getLabel();
			switch(label.getType()) {
			case Constant: 
				r = BinaryTree.binary(label.asConstant().getValue(), 
						this.getLeft().valuesToVariables(), 
						this.getRight().valuesToVariables()); 
				break;
			case Variable: 
				r = BinaryTree.binary(label.asVariable().getValue(), 
						this.getLeft().valuesToVariables(), 
						this.getRight().valuesToVariables()); 
				break;
			}
		}
		return r;
	}
	
	public BinaryTree<E> valuesToVariables(Map<String,E> labels, Map<String,BinaryTree<E>> trees){
		BinaryTree<E> r = null;
		switch(this.getType()) {
		case Empty: r = BinaryTree.empty();; break;
		case Variable: r = trees.get(this.asVariable().getName()); break;		
		case Leaf:
			Label<E> label = this.getLabel();
			switch(label.getType()) {
			case Constant: r = BinaryTree.leaf(label.asConstant().getValue()); break;
			case Variable: r = BinaryTree.leaf(labels.get(label.asVariable().getName())); break;
			}
		case Binary: 
			label = this.getLabel();
			switch(label.getType()) {
			case Constant: 
				r = BinaryTree.binary(label.asConstant().getValue(), 
						this.getLeft().valuesToVariables(labels,trees), 
						this.getRight().valuesToVariables(labels,trees)); 
				break;
			case Variable: 
				r = BinaryTree.binary(labels.get(label.asVariable().getName()), 
						this.getLeft().valuesToVariables(labels,trees), 
						this.getRight().valuesToVariables(labels,trees)); break;
			}
		}
		return r;
	}
	
	
	
	public static <E> BinaryTree<E> transform(BinaryTree<E> tree, BinaryPattern<E> pattern, BinaryPattern<E> result){
		BinaryTree<E> r = tree;
		pattern.reset();
		BinaryPattern<E> m = tree.match(pattern);
		if(m != null) r = result.valuesToVariables(m.varLabels(), m.varTrees());
		return r;
	}
	
	static class Label<E> {
		
		public static <E> Label<E> constLabel(E value){
			return new Label<>(value);
		}
		
		public static <E> Label<E> varLabel(String  name){
			return new Label<>(name);
		}
		
		public enum LabelType{Constant,Variable};
		
		private LabelType type;
		private ConstantExp<E> constant;
		private VariableExp<E> variable;
		
		public Label(E value) {
			super();
			this.constant = ConstantExp.constant(value);
			this.variable = null;
			this.type = LabelType.Constant;
		}
		
		public Label(String name) {
			super();
			this.constant = null;
			this.variable = VariableExp.variable(name);
			this.type = LabelType.Variable;
		}

		public LabelType getType() {
			return type;
		}
		
		public Boolean isConst() {
			return type == LabelType.Constant;
		}

		public Boolean isVariable() {
			return type == LabelType.Variable;
		}
		
		public ConstantExp<E> asConstant(){
			return constant;
		}
		
		public VariableExp<E> asVariable(){
			return variable;
		}
		
		@Override
		public String toString() {
			String s = null;
			switch(type) {
			case Constant: s = this.asConstant().getValue().toString(); break;
			case Variable: s = this.asVariable().getName(); break;
			}
			return s;
		}
		
		
	}
    
    
	
	public static void main(String[] args) {
		BinaryTree<Integer> t1 = BinaryTree.empty();
		BinaryTree<Integer> t2 = BinaryTree.leaf(2);
		BinaryTree<Integer> t3 = BinaryTree.leaf(3);
		BinaryTree<Integer> t4 = BinaryTree.leaf(4);
		BinaryTree<Integer> t5 = BinaryTree.binary(27,BinaryTree.binary(42,t1,t2),BinaryTree.binary(59,t3,t4));
		BinaryTree<Integer> t6 = BinaryTree.binary(39, t2,t5);
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t6);
		
		System.out.println("__________________");
		 
		BinaryTree<Double> t = BinaryTree.parse("-43.7(2.1,56(-27.3(_,2),78.2(3,4)))", x->Double.parseDouble(x));		
		BinaryPattern<Double> p = BinaryPattern.parse("-43.7(2.1,56(_e0(_,2),_T0))",x->Double.parseDouble(x));
		BinaryPattern<Double> r = BinaryPattern.parse("-43.7(_T0,_e0(_e0(_,_T0),_T0))",x->Double.parseDouble(x));
//		BinaryPattern<Double> q = BinaryPattern.parse("_d0(_T0,_c0(_e0(_,_T0),_T0))",x->Double.parseDouble(x));
//        BinaryPattern<Double> leftRight = BinaryPattern.parse("_e5(_e3(_A,_e4(_B,_C)),_D)");
//        BinaryTree<Double> tree1 = BinaryTree.parse("54.5(39.2(20.1,50.5(40.2,51.0)),78.9)",x->Double.parseDouble(x));
//        BinaryPattern<Double> rightLeft = BinaryPattern.parse("_e3(_A,_e5(_e4(_B,_C),_D))");
//		BinaryTree<Double> tree2 = BinaryTree.parse("54.5(39.2,20.1(50.5(40.2,51.0),78.9))",x->Double.parseDouble(x));
//        BinaryPattern<Double> leftLeft = BinaryPattern.parse("_e5(_e4(_e3(_A,_B),_C),_D)");
//        BinaryTree<Double> tree3 = BinaryTree.parse("54.5(39.2(20.1(50.5,40.2),51.0),78.9)",x->Double.parseDouble(x));
//        BinaryPattern<Double> rightRight = BinaryPattern.parse("_e3(_A,_e4(_B,_e5(_C,_D)))");
//        BinaryTree<Double> tree4 = BinaryTree.parse("54.5(39.2,20.1(50.5,40.2(51.0,78.9)))",x->Double.parseDouble(x));
//        BinaryPattern<Double> result = BinaryPattern.parse("_e4(_e3(_A,_B),_e5(_C,_D))");
		
		
        System.out.println("__________________");
        System.out.println(t);
        System.out.println(p);
		System.out.println(r);
		System.out.println("Aqui 0 = "+t.transform(p, r));
//		System.out.println("__________________");
//		System.out.println(tree1);
//		System.out.println(leftRight);
//		System.out.println(result);
//		System.out.println("Aqui 1 = " + tree1.transform(leftRight, result));
//		System.out.println(tree2);
//		System.out.println(rightLeft);
//		System.out.println(rightLeft.varLabels()+"----"+rightLeft.varTrees());
//		System.out.println(result);	
//		System.out.println("Aqui 2 = "+tree2.transform(rightLeft, result));
//		System.out.println(tree3);
//		System.out.println(leftLeft);
//		System.out.println(result);
//		System.out.println("Aqui 3 = "+tree3.transform(leftLeft, result));
//		System.out.println(tree4);
//		System.out.println(rightRight);
//		System.out.println(result);
//		System.out.println("Aqui 4 = "+tree4.transform(rightRight, result));
//		System.out.println(map(t,p,r));
//		BinaryPattern<Integer> p0 = BinaryPattern.free("_t0");
//		BinaryPattern<Integer> pt = BinaryPattern.binaryFree("_e0",BinaryPattern.leaf(2),p0);
//		System.out.println(pt);
//		System.out.println(pt.values());
//		System.out.println(match(t6,pt));
//		System.out.println(pt.values());
	}
}
