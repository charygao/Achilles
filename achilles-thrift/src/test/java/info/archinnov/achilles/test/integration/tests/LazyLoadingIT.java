package info.archinnov.achilles.test.integration.tests;

import static info.archinnov.achilles.common.ThriftCassandraDaoTest.getEntityDao;
import static info.archinnov.achilles.table.TableNameNormalizer.normalizerAndValidateColumnFamilyName;
import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.common.ThriftCassandraDaoTest;
import info.archinnov.achilles.dao.ThriftGenericEntityDao;
import info.archinnov.achilles.entity.manager.ThriftEntityManager;
import info.archinnov.achilles.proxy.ThriftEntityInterceptor;
import info.archinnov.achilles.test.integration.entity.CompleteBean;
import info.archinnov.achilles.test.integration.entity.CompleteBeanTestBuilder;
import net.sf.cglib.proxy.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * LazyLoadingIT
 * 
 * @author DuyHai DOAN
 * 
 */
public class LazyLoadingIT
{
    private ThriftEntityManager em = ThriftCassandraDaoTest.getEm();

    private ThriftGenericEntityDao dao = getEntityDao(
            normalizerAndValidateColumnFamilyName(CompleteBean.class.getName()), Long.class);

    private CompleteBean bean;

    @Before
    public void setUp()
    {
        bean = CompleteBeanTestBuilder
                .builder()
                .randomId()
                .name("DuyHai")
                .age(35L)
                .addFriends("foo", "bar")
                .label("label")
                .buid();

        em.persist(bean);
    }

    @Test
    public void should_not_load_lazy_fields() throws Exception
    {
        bean = em.find(CompleteBean.class, bean.getId());

        Factory proxy = (Factory) bean;
        ThriftEntityInterceptor<?> interceptor = (ThriftEntityInterceptor<?>) proxy.getCallback(0);
        CompleteBean trueBean = (CompleteBean) interceptor.getTarget();

        assertThat(trueBean.getLabel()).isNull();
        assertThat(trueBean.getFriends()).isNull();

        // Trigger loading of lazy fields
        assertThat(bean.getLabel()).isEqualTo("label");
        assertThat(bean.getFriends()).containsExactly("foo", "bar");

        assertThat(trueBean.getLabel()).isEqualTo("label");
        assertThat(trueBean.getFriends()).containsExactly("foo", "bar");
    }

    @Test
    public void should_set_lazy_field() throws Exception
    {
        bean = em.find(CompleteBean.class, bean.getId());

        bean.setLabel("newLabel");

        assertThat(bean.getLabel()).isEqualTo("newLabel");
    }

    @After
    public void tearDown()
    {
        dao.truncate();
    }
}