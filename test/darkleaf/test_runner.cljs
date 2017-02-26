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
            [darkleaf.router.section-test]))

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
 'darkleaf.router.section-test)
