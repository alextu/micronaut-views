/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.views.ViewsFilter
import io.micronaut.views.velocity.VelocityViewsRenderer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class VelocityViewRendererSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer,
            [
                    'spec.name': 'velocity',
                    'micronaut.security.enabled': false
            ],
            "test")

    @Shared
    @AutoCleanup
    HttpClient client = embeddedServer.getApplicationContext().createBean(HttpClient, embeddedServer.getURL())

    def "bean is loaded"() {
        when:
        embeddedServer.applicationContext.getBean(VelocityViewsRenderer)
        embeddedServer.applicationContext.getBean(ViewsFilter)

        then:
        noExceptionThrown()
    }

    def "invoking /velocity/home does not specify @View, thus, regular JSON rendering is used"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/home', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("{\"username\":\"sdelamo\",\"loggedIn\":true}")
        rsp.contentType.isPresent()
        rsp.contentType.get() == MediaType.APPLICATION_JSON_TYPE
    }

    def "invoking /velocity renders velocity template from a controller returning a map"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("<h1>username: <span>sdelamo</span></h1>")
    }

    def "invoking /velocity/pogo renders velocity template from a controller returning a pogo"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/pogo', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("<h1>username: <span>sdelamo</span></h1>")
    }

    def "invoking /velocity/reactive renders velocity template from a controller returning a reactive type"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/reactive', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("<h1>username: <span>sdelamo</span></h1>")
    }

    def "invoking /velocity/modelAndView renders velocity template from a controller returning a ModelAndView instance"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/modelAndView', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("<h1>username: <span>sdelamo</span></h1>")
    }

    def "invoking /velocity/plainText renders with template but sets content type to txt/plain"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/plainText', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK
        rsp.getHeaders().getContentType().isPresent()
        rsp.getHeaders().getContentType().get() == MediaType.TEXT_PLAIN

        when:
        String body = rsp.body()

        then:
        body
        body.contains("<h1>username: <span>sdelamo</span></h1>")
    }

    def "invoking /velocity/bogus returns 404 if you attempt to render a template which does not exist"() {
        when:
        client.toBlocking().exchange('/velocity/bogus', String)

        then:
        HttpClientResponseException e = thrown()

        and:
        e.status == HttpStatus.INTERNAL_SERVER_ERROR
    }

    def "invoking /velocity/nullbody renders view even if the response body is null"() {
        when:
        HttpResponse<String> rsp = client.toBlocking().exchange('/velocity/nullbody', String)

        then:
        noExceptionThrown()
        rsp.status() == HttpStatus.OK

        when:
        String body = rsp.body()

        then:
        body
        rsp.body().contains("<h1>You are not logged in</h1>")
    }
}
