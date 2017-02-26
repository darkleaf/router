(ns darkleaf.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [darkleaf.router.additional-request-keys-test]
            [darkleaf.router.args-test]
            [darkleaf.router.async-test]
            [darkleaf.router.group-test]
            [darkleaf.router.guard-test]
            [darkleaf.router.mount-test]
            [darkleaf.router.pass-test]
            [darkleaf.router.resource-test]
            [darkleaf.router.resources-test]
            [darkleaf.router.section-test]
            [darkleaf.router.html.method-override-test]
            [darkleaf.router.use-cases.resource-composition-test]
            [darkleaf.router.use-cases.member-middleware-test]
            [darkleaf.router.use-cases.domain-constraint-test]))

(doo-tests
 'darkleaf.router.additional-request-keys-test
 'darkleaf.router.args-test
 'darkleaf.router.async-test
 'darkleaf.router.group-test
 'darkleaf.router.guard-test
 'darkleaf.router.mount-test
 'darkleaf.router.pass-test
 'darkleaf.router.resource-test
 'darkleaf.router.resources-test
 'darkleaf.router.section-test
 'darkleaf.router.html.method-override-test
 'darkleaf.router.use-cases.resource-composition-test
 'darkleaf.router.use-cases.member-middleware-test
 'darkleaf.router.use-cases.domain-constraint-test)
