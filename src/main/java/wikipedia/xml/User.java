package wikipedia.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false)
class User {

	@Attribute
	private String editcount;

	@Attribute
	private String registration;

	@Attribute
	private String name;

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(final String registration) {
		this.registration = registration;
	}

	public String getEditcount() {
		return editcount;
	}

	public void setEditcount(final String editcount) {
		this.editcount = editcount;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
}