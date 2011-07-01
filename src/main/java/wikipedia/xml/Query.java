package wikipedia.xml;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * XML Element returned from Wikipedia API
 * see http://en.wikipedia.org/w/api.php
 */
@Root(strict = false)
public final class Query {

    @ElementList(required = false)
    private List<Page> pages;

    @ElementList(required = false)
    private List<User> users;

    @ElementList(required = false)
    private List<CategoryMember> categorymembers;

    @ElementList(required = false)
    private List<Search> search;

    public List<Search> getSearch() {
        return search;
    }

    public void setSearch(final List<Search> search) {
        this.search = search;
    }

    public List<CategoryMember> getCategorymembers() {
        return categorymembers;
    }

    public void setCategorymembers(final List<CategoryMember> categorymembers) {
        this.categorymembers = categorymembers;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(final List<Page> pages) {
        this.pages = pages;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }
}
