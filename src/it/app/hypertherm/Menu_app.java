package it.app.hypertherm;

public class Menu_app {

	private String item;
	private boolean menuFlaggato;

	public Menu_app(String item, boolean flaggato) {
		this.item = item;
		this.menuFlaggato = flaggato;
	}

	public Menu_app() {

	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Boolean getMenuFlaggato() {
		return menuFlaggato;
	}
}