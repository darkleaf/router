(ns darkleaf.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [darkleaf.router-test]
            [darkleaf.router-async-test]
            [darkleaf.router.args-test]))

(doo-tests 'darkleaf.router-test
           'darkleaf.router-async-test
           'darkleaf.router.args-test)
